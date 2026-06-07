package com.pharmacy.catalog_service.dto;

import java.math.BigDecimal;

/**
 * Response trả về giá đã tính cho tier và số lượng
 */
public record PriceResponse(
        Long medicineId,
        String tierCode,
        String saleMode,
        String unitCode,
        String unitLabel,
        Integer conversionFactor,
        Integer qty,
        BigDecimal unitPrice,
        BigDecimal discountPercent,
        BigDecimal finalUnitPrice,
        BigDecimal totalPrice
) {
    public static PriceResponse of(Long medicineId, String tierCode, Integer qty,
                                    BigDecimal unitPrice, BigDecimal discountPercent) {
        return of(medicineId, tierCode, tierCode, null, null, 1, qty, unitPrice, discountPercent);
    }

    public static PriceResponse of(Long medicineId, String tierCode, String saleMode,
                                   String unitCode, String unitLabel, Integer conversionFactor,
                                   Integer qty, BigDecimal unitPrice, BigDecimal discountPercent) {
        BigDecimal discount = discountPercent != null ? discountPercent : BigDecimal.ZERO;
        BigDecimal multiplier = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100)));
        BigDecimal finalUnit = unitPrice.multiply(multiplier);
        BigDecimal total = finalUnit.multiply(BigDecimal.valueOf(qty));

        return new PriceResponse(
                medicineId,
                tierCode,
                saleMode,
                unitCode,
                unitLabel,
                conversionFactor != null && conversionFactor > 0 ? conversionFactor : 1,
                qty,
                unitPrice,
                discount,
                finalUnit,
                total
        );
    }
}
