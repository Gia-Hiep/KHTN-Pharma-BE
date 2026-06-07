package com.pharmacy.notification_service.dto;

import jakarta.validation.constraints.NotNull;

public record CreateNotificationRequest(
        @NotNull Long userId,
        String type,
        String title,
        String message,
        Long referenceId
) {}
