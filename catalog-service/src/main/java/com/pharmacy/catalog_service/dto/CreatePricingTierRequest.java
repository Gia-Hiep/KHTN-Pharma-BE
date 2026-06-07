package com.pharmacy.catalog_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePricingTierRequest(
        @NotNull(message = "medicineId is required")
        Long medicineId,

        @NotBlank(message = "tierCode is required")
        String tierCode,

        @Min(value = 1, message = "minQty must be >= 1")
        Integer minQty,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be > 0")
        BigDecimal price,

        @DecimalMin(value = "0", message = "discountPercent must be >= 0")
        BigDecimal discountPercent,

        @NotNull(message = "effectiveFrom is required")
        LocalDateTime effectiveFrom,

        LocalDateTime effectiveTo
) {
    public CreatePricingTierRequest {
        if (minQty == null) minQty = 1;
        if (discountPercent == null) discountPercent = BigDecimal.ZERO;
    }
}
