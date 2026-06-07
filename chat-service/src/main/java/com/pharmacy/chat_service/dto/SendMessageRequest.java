package com.pharmacy.chat_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(
        @NotNull(message = "conversationId không được null")
        Long conversationId,

        @NotBlank(message = "content không được trống")
        String content,

        String messageType,  // TEXT, IMAGE, FILE (default: TEXT)
        String attachmentUrl,
        String attachmentName
) {}
