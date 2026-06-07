package com.pharmacy.chat_service.dto;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        String type,
        Long participant1,
        Long participant2,
        String title,
        String status,
        LocalDateTime lastMessageAt,
        LocalDateTime createdAt,
        Long unreadCount,
        MessageResponse lastMessage
) {}
