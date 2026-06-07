package com.pharmacy.chat_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateConversationRequest(
        Long participantId,   // Optional: null khi BUYER tạo support ticket

        @NotBlank(message = "type không được trống")
        String type,

        String title
) {}

