package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.dto.ChatRequest;
import com.upsc.upsctracker.service.AIChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
public class ChatbotController {

    private final AIChatService aiChatService;

    public ChatbotController(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    // =========================================================
    // CHATBOT PAGE
    // =========================================================

    @GetMapping("/chatbot")
    public String chatbotPage() {
        return "chatbot";
    }

    // =========================================================
    // SEND NORMAL MESSAGE
    // =========================================================

    @PostMapping("/api/chat/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(
            @RequestBody(required = false) ChatRequest request,
            HttpSession session) {

        Integer userId = getLoggedInUserId(session);

        if (userId == null) {
            return unauthorized();
        }

        if (request == null) {
            return badRequest("Request cannot be empty.");
        }

        String message = request.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return badRequest("Message cannot be empty.");
        }

        try {

            AIChatService.ChatResult result =
                    aiChatService.sendMessage(
                            userId,
                            request.getConversationId(),
                            message.trim(),
                            request.getConversationType()
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "conversationId", result.conversationId(),
                            "conversationType", result.conversationType(),
                            "reply", result.reply()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to process your message."
                    ));
        }
    }

    // =========================================================
    // SEND MESSAGE WITH FILE
    // =========================================================

    @PostMapping(
            value = "/api/chat/send-with-file",
            consumes = "multipart/form-data"
    )
    @ResponseBody
    public ResponseEntity<?> sendMessageWithFile(
            @RequestParam(value = "conversationId", required = false)
            Long conversationId,

            @RequestParam(
                    value = "message",
                    required = false,
                    defaultValue = ""
            )
            String message,

            @RequestParam(
                    value = "conversationType",
                    required = false,
                    defaultValue = "DOUBT"
            )
            String conversationType,

            @RequestPart(
                    value = "file",
                    required = false
            )
            MultipartFile file,

            HttpSession session) {

        Integer userId = getLoggedInUserId(session);

        if (userId == null) {
            return unauthorized();
        }

        if ((message == null || message.trim().isEmpty())
                && (file == null || file.isEmpty())) {

            return badRequest(
                    "Please enter a message or attach a file."
            );
        }

        try {

            byte[] fileBytes = null;
            String fileName = null;
            String mimeType = null;

            if (file != null && !file.isEmpty()) {

                fileBytes = file.getBytes();

                fileName = file.getOriginalFilename();

                mimeType = file.getContentType();

                if (mimeType == null || mimeType.isBlank()) {
                    mimeType = "application/octet-stream";
                }
            }

            AIChatService.ChatResult result =
                    aiChatService.sendMessage(
                            userId,
                            conversationId,
                            message == null ? "" : message.trim(),
                            conversationType,
                            fileBytes,
                            mimeType,
                            fileName
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "conversationId", result.conversationId(),
                            "conversationType", result.conversationType(),
                            "reply", result.reply()
                    )
            );

        } catch (IllegalArgumentException e) {

            return badRequest(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message",
                            "Failed to process your message and attachment."
                    ));
        }
    }

    // =========================================================
    // GET CHAT HISTORY
    // =========================================================

    @GetMapping("/api/chat/conversations")
    @ResponseBody
    public ResponseEntity<?> getConversations(
            HttpSession session) {

        Integer userId = getLoggedInUserId(session);

        if (userId == null) {
            return unauthorized();
        }

        try {

            return ResponseEntity.ok(
                    aiChatService.getUserConversations(userId)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message",
                            "Failed to load conversations."
                    ));
        }
    }

    // =========================================================
    // GET MESSAGES OF ONE CONVERSATION
    // =========================================================

    @GetMapping(
            "/api/chat/conversations/{conversationId}/messages"
    )
    @ResponseBody
    public ResponseEntity<?> getConversationMessages(
            @PathVariable Long conversationId,
            HttpSession session) {

        Integer userId = getLoggedInUserId(session);

        if (userId == null) {
            return unauthorized();
        }

        try {

            return ResponseEntity.ok(
                    aiChatService.getConversationMessages(
                            userId,
                            conversationId
                    )
            );

        } catch (IllegalArgumentException e) {

            return badRequest(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message",
                            "Failed to load conversation."
                    ));
        }
    }

    // =========================================================
    // DELETE CHAT CONVERSATION
    // =========================================================

    @DeleteMapping(
            "/api/chat/conversations/{conversationId}"
    )
    @ResponseBody
    public ResponseEntity<?> deleteConversation(
            @PathVariable Long conversationId,
            HttpSession session) {

        Integer userId = getLoggedInUserId(session);

        if (userId == null) {
            return unauthorized();
        }

        try {

            aiChatService.deleteConversation(
                    userId,
                    conversationId
            );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message",
                            "Conversation deleted successfully."
                    )
            );

        } catch (IllegalArgumentException e) {

            return badRequest(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message",
                            "Failed to delete conversation."
                    ));
        }
    }

    // =========================================================
    // GET LOGGED-IN USER ID
    // =========================================================

    private Integer getLoggedInUserId(HttpSession session) {

        Object value = session.getAttribute("loggedInUserId");

        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Long) {
            return ((Long) value).intValue();
        }

        if (value instanceof String) {

            try {
                return Integer.parseInt((String) value);

            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    // =========================================================
    // ERROR RESPONSES
    // =========================================================

    private ResponseEntity<?> unauthorized() {

        return ResponseEntity.status(401)
                .body(
                        Map.of(
                                "success", false,
                                "message", "Please login first."
                        )
                );
    }

    private ResponseEntity<?> badRequest(String message) {

        return ResponseEntity.badRequest()
                .body(
                        Map.of(
                                "success", false,
                                "message",
                                message == null
                                        ? "Invalid request."
                                        : message
                        )
                );
    }
}