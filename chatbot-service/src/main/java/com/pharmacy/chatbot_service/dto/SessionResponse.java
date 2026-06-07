package com.pharmacy.chatbot_service.dto;

import com.pharmacy.chatbot_service.entity.BotMessage;
import com.pharmacy.chatbot_service.entity.BotSession;

import java.time.LocalDateTime;
import java.util.List;

public record SessionResponse(
        Long sessionId,
        String sessionToken,
        Long userId,
        String status,
        String currentIntent,
        Integer fallbackCount,
        LocalDateTime createdAt,
        LocalDateTime lastActiveAt,
        List<BotMessage> messages
) {
    public static SessionResponse from(BotSession session, List<BotMessage> messages) {
        return new SessionResponse(
                session.getId(),
                session.getSessionToken(),
                session.getUserId(),
                session.getStatus(),
                session.getCurrentIntent(),
                session.getFallbackCount(),
                session.getCreatedAt(),
                session.getLastActiveAt(),
                messages
        );
    }
}
