package com.pharmacy.sales_service.dto;

import java.math.BigDecimal;
import java.util.List;

/** Request thêm sản phẩm vào giỏ hàng */
public record AddCartItemRequest(
        Long medicineId,
        String medicineName,
        String imageUrl,
        Integer qty,
        String unitCode,
        String unitLabel,
        Integer conversionFactor,
        BigDecimal unitPrice,
        String priceTier,
        String saleMode
) {}
