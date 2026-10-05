package com.upsc.upsctracker.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.File;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.UploadFileConfig;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OpenAIService {

    private static final String MODEL_NAME =
            "gemini-3.6-flash";

    /*
     * Keep normal answers reasonably sized.
     *
     * This helps avoid unnecessary generation time
     * for simple UPSC chatbot questions.
     */
    private static final int MAX_OUTPUT_TOKENS = 1200;

    private final Client client;


    public OpenAIService() {

        String apiKey =
                System.getenv("GEMINI_API_KEY");


        if (apiKey == null || apiKey.isBlank()) {

            throw new IllegalStateException(
                    "GEMINI_API_KEY environment variable is not set."
            );
        }


        client =
                Client.builder()
                        .apiKey(apiKey)
                        .build();
    }


    // =========================================================
    // TEXT ONLY RESPONSE
    // =========================================================

    public String generateResponse(
            String systemPrompt,
            String userMessage) {

        String safeSystemPrompt =
                systemPrompt == null
                        ? ""
                        : systemPrompt.trim();


        String safeUserMessage =
                userMessage == null
                        ? ""
                        : userMessage.trim();


        /*
         * Put system instructions in Gemini's dedicated
         * systemInstruction field instead of embedding them
         * inside the normal user message.
         *
         * Gemini officially supports this configuration.
         */

        GenerateContentConfig config =
                buildGenerationConfig(
                        safeSystemPrompt
                );


        GenerateContentResponse response =
                client.models.generateContent(
                        MODEL_NAME,
                        safeUserMessage,
                        config
                );


        return extractResponseText(
                response
        );
    }


    // =========================================================
    // TEXT + FILE / IMAGE / PDF RESPONSE
    // =========================================================

    public String generateResponse(
            String systemPrompt,
            String userMessage,
            byte[] fileBytes,
            String mimeType,
            String fileName) {

        if (
                fileBytes == null
                        ||
                        fileBytes.length == 0
        ) {

            return generateResponse(
                    systemPrompt,
                    userMessage
            );
        }


        if (
                mimeType == null
                        ||
                        mimeType.isBlank()
        ) {

            mimeType =
                    "application/octet-stream";
        }


        String safeSystemPrompt =
                systemPrompt == null
                        ? ""
                        : systemPrompt.trim();


        String safeUserMessage =
                userMessage == null
                        ? ""
                        : userMessage.trim();


        String safeFileName =
                fileName == null
                        || fileName.isBlank()
                        ? "attachment"
                        : fileName;


        /*
         * Common instruction for all attachments.
         */

        String attachmentInstruction = """
                The user has attached a file named "%s".

                Analyze the attached file carefully.
                Use the contents of the file when answering
                the user's request.

                If it is a PDF, understand the relevant
                text, tables, pages and information before
                answering.

                Do not claim that you cannot access the file
                if the file was successfully provided.
                """.formatted(
                safeFileName
        );


        /*
         * =====================================================
         * IMAGE
         * =====================================================
         *
         * Images can be sent directly as bytes.
         */

        if (mimeType.startsWith("image/")) {

            Content content =
                    Content.fromParts(

                            Part.fromText(
                                    combinePrompt(
                                            safeUserMessage,
                                            attachmentInstruction
                                    )
                            ),

                            Part.fromBytes(
                                    fileBytes,
                                    mimeType
                            )
                    );


            GenerateContentConfig config =
                    buildGenerationConfig(
                            safeSystemPrompt
                    );


            GenerateContentResponse response =
                    client.models.generateContent(
                            MODEL_NAME,
                            content,
                            config
                    );


            return extractResponseText(
                    response
            );
        }


        /*
         * =====================================================
         * PDF / TXT / CSV / OTHER DOCUMENT
         * =====================================================
         *
         * Gemini Files API is used for these files.
         */

        Path temporaryFile = null;


        try {

            String safeTemporaryName =
                    createSafeFileName(
                            safeFileName,
                            mimeType
                    );


            temporaryFile =
                    Files.createTempFile(
                            "upsc-chat-",
                            "-" + safeTemporaryName
                    );


            Files.write(
                    temporaryFile,
                    fileBytes
            );


            /*
             * Upload the local temporary file to Gemini.
             */

            File uploadedFile =
                    client.files.upload(
                            temporaryFile.toFile(),
                            UploadFileConfig
                                    .builder()
                                    .mimeType(mimeType)
                                    .build()
                    );


            String fileUri =
                    uploadedFile
                            .uri()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Gemini did not return a file URI."
                                            )
                            );


            String uploadedMimeType =
                    uploadedFile
                            .mimeType()
                            .orElse(mimeType);


            /*
             * Build multimodal content.
             */

            Content content =
                    Content.fromParts(

                            Part.fromText(
                                    combinePrompt(
                                            safeUserMessage,
                                            attachmentInstruction
                                    )
                            ),

                            Part.fromUri(
                                    fileUri,
                                    uploadedMimeType
                            )
                    );


            /*
             * Use the same low-latency generation configuration.
             */

            GenerateContentConfig config =
                    buildGenerationConfig(
                            safeSystemPrompt
                    );


            GenerateContentResponse response =
                    client.models.generateContent(
                            MODEL_NAME,
                            content,
                            config
                    );


            return extractResponseText(
                    response
            );


        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to prepare the attachment for Gemini.",
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Gemini could not process the attached file: "
                            + e.getMessage(),
                    e
            );

        } finally {

            /*
             * Delete only our local temporary copy.
             */

            if (temporaryFile != null) {

                try {

                    Files.deleteIfExists(
                            temporaryFile
                    );

                } catch (IOException ignored) {

                    // Nothing to do.
                }
            }
        }
    }


    // =========================================================
    // GENERATION CONFIG
    // =========================================================

    private GenerateContentConfig buildGenerationConfig(
            String systemPrompt) {

        GenerateContentConfig.Builder builder =
                GenerateContentConfig.builder();


        /*
         * Dedicated system instructions.
         */

        if (
                systemPrompt != null
                        &&
                        !systemPrompt.isBlank()
        ) {

            builder.systemInstruction(
                    Content.fromParts(
                            Part.fromText(
                                    systemPrompt
                            )
                    )
            );
        }


        /*
         * Gemini 3.6 Flash supports minimal thinking.
         *
         * This is useful for a conversational UPSC
         * assistant where low latency matters more than
         * deep multi-step reasoning for every question.
         */

        builder.thinkingConfig(
                ThinkingConfig.builder()
                        .thinkingLevel("minimal")
                        .build()
        );


        /*
         * Prevent unnecessarily long answers.
         */

        builder.maxOutputTokens(
                MAX_OUTPUT_TOKENS
        );


        return builder.build();
    }


    // =========================================================
    // COMBINE USER MESSAGE + ATTACHMENT INSTRUCTION
    // =========================================================

    private String combinePrompt(
            String userMessage,
            String attachmentInstruction) {

        if (
                userMessage == null
                        ||
                        userMessage.isBlank()
        ) {

            return attachmentInstruction;
        }


        return """
                USER MESSAGE:

                %s

                %s
                """.formatted(
                userMessage,
                attachmentInstruction
        );
    }


    // =========================================================
    // CREATE SAFE TEMPORARY FILE NAME
    // =========================================================

    private String createSafeFileName(
            String fileName,
            String mimeType) {

        String name =
                fileName == null
                        ? "attachment"
                        : fileName.trim();


        if (name.isBlank()) {

            name =
                    "attachment";
        }


        /*
         * Remove characters that are not suitable
         * for a temporary filename.
         */

        name =
                name.replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );


        /*
         * Make sure an extension exists for
         * common supported document types.
         */

        if (
                !name.contains(".")
                        &&
                        mimeType != null
        ) {

            if (
                    mimeType.equals(
                            "application/pdf"
                    )
            ) {

                name += ".pdf";

            } else if (
                    mimeType.equals(
                            "text/plain"
                    )
            ) {

                name += ".txt";

            } else if (
                    mimeType.equals(
                            "text/csv"
                    )
            ) {

                name += ".csv";
            }
        }


        return name;
    }


    // =========================================================
    // RESPONSE EXTRACTION
    // =========================================================

    private String extractResponseText(
            GenerateContentResponse response) {

        if (response == null) {

            return "Sorry, I couldn't generate a response.";
        }


        String responseText =
                response.text();


        if (
                responseText == null
                        ||
                        responseText.isBlank()
        ) {

            return "Sorry, I couldn't generate a response.";
        }


        return responseText.trim();
    }
}