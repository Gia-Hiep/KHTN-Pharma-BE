package com.pharmacy.customer_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateCouponRequest(
        @NotBlank(message = "code is required")
        String code,

        @NotBlank(message = "discountType is required")
        String discountType, // PERCENT or FIXED

        @NotNull(message = "discountValue is required")
        @DecimalMin(value = "0.01", message = "discountValue must be > 0")
        BigDecimal discountValue,

        BigDecimal minOrderAmount,
        BigDecimal maxDiscount,
        Integer usageLimit,

        @NotNull(message = "validFrom is required")
        LocalDateTime validFrom,

        @NotNull(message = "validTo is required")
        LocalDateTime validTo,

        String customerTier,
        String customerType,
        String description
) {}
