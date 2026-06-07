package com.pharmacy.customer_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDebtRequest(
        @NotNull(message = "customerId is required")
        Long customerId,

        @NotNull(message = "invoiceId is required")
        Long invoiceId,

        String invoiceCode,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be > 0")
        BigDecimal amount,

        @NotNull(message = "dueDate is required")
        LocalDate dueDate,

        String notes
) {}
