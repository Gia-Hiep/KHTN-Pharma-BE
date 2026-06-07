package com.pharmacy.chat_service.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String content,
        String messageType,
        String attachmentUrl,
        String attachmentName,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {}
