package com.pharmacy.chatbot_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
        @NotNull Long sessionId,
        @NotNull @Min(1) @Max(5) Integer rating,
        String comment,
        Boolean resolved
) {}
