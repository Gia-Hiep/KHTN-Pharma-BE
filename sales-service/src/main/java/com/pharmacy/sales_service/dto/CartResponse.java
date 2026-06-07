package com.pharmacy.sales_service.dto;

import java.math.BigDecimal;
import java.util.List;

/** Response trả về giỏ hàng */
public record CartResponse(
        Long id,
        Long buyerId,
        List<CartItemResponse> items,
        int totalQty,
        BigDecimal totalAmount
) {
    public record CartItemResponse(
            Long id,
            Long medicineId,
            String medicineName,
            String imageUrl,
            Integer qty,
            String unitCode,
            String unitLabel,
            Integer conversionFactor,
            BigDecimal unitPrice,
            String priceTier,
            String saleMode,
            BigDecimal lineTotal
    ) {}
}
