package com.pharmacy.chatbot_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFaqRequest(
        @NotBlank String intent,
        @NotBlank String keywords,
        @NotBlank String question,
        @NotBlank String answer,
        Integer priority
) {}
