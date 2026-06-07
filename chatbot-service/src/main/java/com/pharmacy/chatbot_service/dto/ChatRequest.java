package com.pharmacy.chatbot_service.dto;

/**
 * Request body cho POST /chatbot/query.
 * - Nếu có actionId → route trực tiếp, message optional.
 * - Nếu không có actionId → cần message.
 */
public record ChatRequest(
        String message,
        String actionId,
        Long sessionId,
        String sessionToken   // backward compat with legacy /chatbot/message
) {}
