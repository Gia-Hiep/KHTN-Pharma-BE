package com.pharmacy.catalog_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request để lấy giá đúng cho tier và số lượng
 */
public record GetPriceRequest(
        @NotNull(message = "medicineId is required")
        Long medicineId,

        @NotBlank(message = "tierCode is required")
        String tierCode,

        @Min(value = 1, message = "qty must be >= 1")
        Integer qty
) {
    public GetPriceRequest {
        if (qty == null) qty = 1;
    }
}
