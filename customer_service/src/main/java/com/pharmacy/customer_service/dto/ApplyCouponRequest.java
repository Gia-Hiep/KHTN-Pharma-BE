package com.pharmacy.customer_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApplyCouponRequest(
        @NotBlank(message = "couponCode is required")
        String couponCode,

        @NotNull(message = "customerId is required")
        Long customerId,

        @NotNull(message = "invoiceId is required")
        Long invoiceId,

        @NotNull(message = "orderAmount is required")
        @DecimalMin(value = "0.01", message = "orderAmount must be > 0")
        BigDecimal orderAmount
) {}
