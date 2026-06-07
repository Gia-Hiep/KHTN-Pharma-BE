package com.pharmacy.sales_service.dto;

import java.util.List;

/**
 * Bước 3 (PICKING): Dược sĩ báo thiếu hàng / tick đã lấy.
 * Hỗ trợ Partial Fulfillment — giảm SL hoặc hủy item.
 */
public record PartialFulfillmentRequest(
        List<ItemUpdate> items
) {
    public record ItemUpdate(
            Long itemId,          // ID của BuyerOrderItem
            Integer actualQty,    // SL thực lấy được (null = giữ nguyên, 0 = hủy item)
            Boolean fulfilled     // true = đã lấy xong item này
    ) {}
}
