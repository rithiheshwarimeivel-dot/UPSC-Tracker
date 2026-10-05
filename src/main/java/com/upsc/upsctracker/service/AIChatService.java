package com.upsc.upsctracker.service;

import com.upsc.upsctracker.entity.AIConversation;
import com.upsc.upsctracker.entity.AIMessage;
import com.upsc.upsctracker.entity.User;
import com.upsc.upsctracker.repository.AIConversationRepository;
import com.upsc.upsctracker.repository.AIMessageRepository;
import com.upsc.upsctracker.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AIChatService {

    private final OpenAIService openAIService;

    private final AIConversationRepository conversationRepository;

    private final AIMessageRepository messageRepository;

    private final UserRepository userRepository;

    public AIChatService(
            OpenAIService openAIService,
            AIConversationRepository conversationRepository,
            AIMessageRepository messageRepository,
            UserRepository userRepository) {

        this.openAIService = openAIService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // NORMAL TEXT MESSAGE
    // =========================================================

    @Transactional
    public ChatResult sendMessage(
            Integer userId,
            Long conversationId,
            String message,
            String conversationType) {

        return sendMessage(
                userId,
                conversationId,
                message,
                conversationType,
                null,
                null,
                null
        );
    }

    // =========================================================
    // MESSAGE WITH FILE
    // =========================================================

    @Transactional
    public ChatResult sendMessage(
            Integer userId,
            Long conversationId,
            String message,
            String conversationType,
            byte[] fileBytes,
            String fileMimeType,
            String fileName) {

        boolean hasMessage =
                message != null && !message.isBlank();

        boolean hasFile =
                fileBytes != null && fileBytes.length > 0;

        if (!hasMessage && !hasFile) {
            throw new IllegalArgumentException(
                    "Message or attachment is required."
            );
        }

        if (hasFile) {
            validateFile(
                    fileBytes,
                    fileMimeType,
                    fileName
            );
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "User not found."
                                )
                        );

        AIConversation conversation;

        if (conversationId != null) {

            conversation =
                    conversationRepository
                            .findById(conversationId)
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Conversation not found."
                                    )
                            );

            verifyOwnership(
                    conversation,
                    userId
            );

        } else {

            conversation = new AIConversation();

            conversation.setUser(user);

            if (
                    conversationType == null
                            || conversationType.isBlank()
            ) {
                conversationType = "DOUBT";
            }

            conversation.setConversationType(
                    conversationType.toUpperCase()
            );

            conversation.setPinned(false);

            conversation =
                    conversationRepository.save(
                            conversation
                    );
        }

        String storedMessage =
                message == null
                        ? ""
                        : message.trim();

        if (hasFile) {

            String attachmentName =
                    fileName == null
                            || fileName.isBlank()
                            ? "attachment"
                            : fileName;

            storedMessage +=
                    "\n[Attachment: "
                            + attachmentName
                            + "]";
        }

        AIMessage userMessage = new AIMessage();

        userMessage.setConversation(
                conversation
        );

        userMessage.setRole("USER");

        userMessage.setMessage(
                storedMessage
        );

        messageRepository.save(userMessage);

        List<AIMessage> previousMessages =
                messageRepository
                        .findByConversationIdOrderByCreatedAtAsc(
                                conversation.getId()
                        );

        String systemPrompt =
                buildSystemPrompt(
                        user,
                        conversation.getConversationType()
                );

        String conversationContext =
                buildConversationContext(
                        previousMessages
                );

        String aiReply;

        if (hasFile) {

            aiReply =
                    openAIService.generateResponse(
                            systemPrompt,
                            conversationContext,
                            fileBytes,
                            fileMimeType,
                            fileName
                    );

        } else {

            aiReply =
                    openAIService.generateResponse(
                            systemPrompt,
                            conversationContext
                    );
        }

        AIMessage assistantMessage =
                new AIMessage();

        assistantMessage.setConversation(
                conversation
        );

        assistantMessage.setRole(
                "ASSISTANT"
        );

        assistantMessage.setMessage(
                aiReply
        );

        messageRepository.save(
                assistantMessage
        );

        return new ChatResult(
                conversation.getId(),
                conversation.getConversationType(),
                aiReply
        );
    }

    // =========================================================
    // FILE VALIDATION
    // =========================================================

    private void validateFile(
            byte[] fileBytes,
            String mimeType,
            String fileName) {

        long maxSize =
                50L * 1024L * 1024L;

        if (fileBytes.length > maxSize) {
            throw new IllegalArgumentException(
                    "File is too large. Maximum size is 50 MB."
            );
        }

        String type =
                mimeType == null
                        ? ""
                        : mimeType.toLowerCase();

        boolean validType =
                type.startsWith("image/")
                        || type.equals("application/pdf")
                        || type.equals("text/plain")
                        || type.equals("text/csv");

        if (!validType) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Please upload an image, PDF, TXT or CSV file."
            );
        }
    }

    // =========================================================
    // PIN / UNPIN CONVERSATION
    // =========================================================

    @Transactional
    public boolean togglePinConversation(
            Integer userId,
            Long conversationId) {

        AIConversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Conversation not found."
                                )
                        );

        verifyOwnership(
                conversation,
                userId
        );

        boolean newPinnedState =
                !conversation.isPinned();

        conversation.setPinned(
                newPinnedState
        );

        conversationRepository.save(
                conversation
        );

        return newPinnedState;
    }

    // =========================================================
    // DELETE CONVERSATION
    // =========================================================

    @Transactional
    public void deleteConversation(
            Integer userId,
            Long conversationId) {

        AIConversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Conversation not found."
                                )
                        );

        verifyOwnership(
                conversation,
                userId
        );

        /*
         * AIMessage contains a foreign key to AIConversation.
         *
         * Therefore messages must be deleted first.
         */

        messageRepository.deleteByConversationId(
                conversationId
        );

        conversationRepository.delete(
                conversation
        );
    }

    // =========================================================
    // VERIFY OWNERSHIP
    // =========================================================

    private void verifyOwnership(
            AIConversation conversation,
            Integer userId) {

        if (
                conversation.getUser() == null
                        || conversation.getUser().getId() == null
                        || conversation
                        .getUser()
                        .getId()
                        .intValue()
                        != userId
        ) {

            throw new IllegalArgumentException(
                    "You are not allowed to access this conversation."
            );
        }
    }

    // =========================================================
    // BUILD SYSTEM PROMPT
    // =========================================================

    private String buildSystemPrompt(
            User user,
            String conversationType) {

        String userName =
                user.getName() == null
                        || user.getName().isBlank()
                        ? "UPSC aspirant"
                        : user.getName().trim();

        String email =
                user.getEmail() == null
                        || user.getEmail().isBlank()
                        ? "Not available"
                        : user.getEmail().trim();

        String attemptYear =
                user.getAttemptYear() == null
                        ? "Not set"
                        : String.valueOf(
                        user.getAttemptYear()
                );

        String whyUpsc =
                user.getWhyUpsc() == null
                        || user.getWhyUpsc().isBlank()
                        ? "Not provided"
                        : user.getWhyUpsc().trim();

        String dreamPost =
                user.getDreamPost() == null
                        || user.getDreamPost().isBlank()
                        ? "Not provided"
                        : user.getDreamPost().trim();

        return """
                You are the AI Study Companion inside the UPSC Tracker application.

                Your main purpose is to help the user prepare effectively
                for the UPSC examination while also acting as a supportive,
                practical and personalized study companion.

                =========================================================
                USER PROFILE
                =========================================================

                User Name:
                %s

                Email:
                %s

                UPSC Attempt Year:
                %s

                Why the user wants to prepare for UPSC:
                %s

                User's Dream Post:
                %s

                =========================================================
                PERSONALIZATION RULES
                =========================================================

                1. Treat the information above as the user's personal
                   UPSC profile.

                2. Use the user's UPSC profile to personalize responses
                   whenever it is relevant.

                3. If the user asks for motivation, feels demotivated,
                   says they do not feel like studying, procrastinating,
                   wants to give up, feels tired of preparation, or asks
                   why they should continue, use their own stated reason
                   for preparing for UPSC as the foundation of the response.

                4. When appropriate, connect their motivation to their
                   selected UPSC attempt year and Dream Post.

                5. Do not repeatedly mention their personal information
                   in every response. Use it naturally and only when
                   relevant.

                6. Never invent a reason for why the user wants UPSC.
                   If their reason is "Not provided", do not pretend
                   that they gave one.

                7. Never invent a Dream Post.
                   If it is "Not provided", do not assume IAS, IPS, IFS
                   or any other service.

                8. Never change or assume the user's attempt year.

                9. If the user asks about their own UPSC goal, use the
                   profile information above.

                10. Be encouraging but realistic. Avoid meaningless
                    motivational clichés.

                =========================================================
                UPSC STUDY RULES
                =========================================================

                1. Give accurate and exam-oriented answers.

                2. Explain difficult concepts in simple language.

                3. For Prelims, focus on facts, concepts, elimination
                   techniques and MCQs.

                4. For Mains, focus on analysis, structure, examples,
                   arguments and conclusions.

                5. For Current Affairs, connect events with the UPSC
                   syllabus where relevant.

                6. For answer evaluation, provide constructive
                   UPSC-oriented feedback.

                7. When useful, distinguish between Prelims and Mains
                   relevance.

                8. Do not unnecessarily make answers extremely long.

                9. If the user asks for a study plan, make it practical
                   and achievable.

                10. If the user is struggling with consistency, help them
                    create a small actionable next step instead of only
                    giving motivation.

                =========================================================
                ATTACHMENT RULES
                =========================================================

                1. If the user uploads an image, inspect it carefully and
                   answer based on its content.

                2. If the user uploads a PDF or text document, analyse
                   the provided material before answering.

                3. Do not claim that you cannot access the file if the
                   file was successfully provided.

                =========================================================
                ACCURACY RULES
                =========================================================

                1. Do not invent facts, sources or examination information.

                2. If information is uncertain or time-sensitive, clearly
                   state the uncertainty.

                3. If the user asks something outside UPSC preparation,
                   answer helpfully when appropriate, but keep the user's
                   overall UPSC preparation context in mind when relevant.

                Conversation Type:
                %s
                """.formatted(
                userName,
                email,
                attemptYear,
                whyUpsc,
                dreamPost,
                conversationType
        );
    }

    // =========================================================
    // BUILD CONVERSATION CONTEXT
    // =========================================================

    private String buildConversationContext(
            List<AIMessage> messages) {

        StringBuilder context =
                new StringBuilder();

        for (AIMessage message : messages) {

            context
                    .append(message.getRole())
                    .append(": ")
                    .append(message.getMessage())
                    .append("\n\n");
        }

        return context.toString();
    }

    // =========================================================
    // GET USER CONVERSATIONS
    // =========================================================

    @Transactional(readOnly = true)
    public List<AIConversation> getUserConversations(
            Integer userId) {

        return conversationRepository
                .findByUserIdOrderByPinnedDescCreatedAtDesc(
                        userId
                );
    }

    // =========================================================
    // GET CONVERSATION MESSAGES
    // =========================================================

    @Transactional(readOnly = true)
    public List<AIMessage> getConversationMessages(
            Integer userId,
            Long conversationId) {

        AIConversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Conversation not found."
                                )
                        );

        verifyOwnership(
                conversation,
                userId
        );

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(
                        conversationId
                );
    }

    // =========================================================
    // RESULT
    // =========================================================

    public record ChatResult(
            Long conversationId,
            String conversationType,
            String reply) {
    }
}