package com.pharmacy.customer_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLoyaltyTransactionRequest(
        @NotNull(message = "customerId is required")
        Long customerId,

        @NotBlank(message = "type is required")
        String type, // EARN, REDEEM, EXPIRE, ADJUST

        @NotNull(message = "points is required")
        Integer points,

        String referenceType,
        String referenceId,
        String note
) {}
