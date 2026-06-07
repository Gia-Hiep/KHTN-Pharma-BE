package com.pharmacy.sales_service.dto;

import java.math.BigDecimal;
import java.util.List;

/** BUYER gửi khi tạo đơn hàng online */
public record CreateOrderRequest(
        String buyerName,
        String shippingAddress,
        String paymentMethod,
        String paymentStatus,   // "PAID" (VietQR đã CK) | "UNPAID" (COD)
        String notes,
        String couponCode,
        String stripePaymentIntentId,
        List<Item> items
) {
    public record Item(
            Long medicineId,
            String medicineName,
            Integer qty,
            String unitCode,
            String unitLabel,
            Integer conversionFactor,
            BigDecimal unitPrice,
            String priceTier,   // compatibility field
            String saleMode
    ) {}
}
