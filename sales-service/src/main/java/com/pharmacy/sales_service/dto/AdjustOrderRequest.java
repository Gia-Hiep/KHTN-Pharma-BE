package com.pharmacy.sales_service.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request để PHARMACIST điều chỉnh (adjust) đơn hàng BUYER trước khi approve.
 * Chỉ cho phép adjust khi đơn ở trạng thái PENDING_APPROVAL.
 */
public record AdjustOrderRequest(
        List<ItemAdjust> items,
        String adjustNote  // ghi chú lý do điều chỉnh
) {
    public record ItemAdjust(
            Long itemId,          // ID của BuyerOrderItem
            Integer newQty,       // số lượng mới (null = giữ nguyên)
            BigDecimal newPrice   // giá mới (null = giữ nguyên)
    ) {}
}
