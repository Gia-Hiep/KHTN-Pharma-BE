package com.pharmacy.customer_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PayDebtRequest(
        @NotNull(message = "debtId is required")
        Long debtId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be > 0")
        BigDecimal amount,

        String notes
) {}
