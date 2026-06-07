package com.pharmacy.sales_service.dto;

/**
 * Request để PHARMACIST tạo vận đơn khi chuyển PACKING → SHIPPING.
 */
public record ShipOrderRequest(
        String carrier,       // đơn vị vận chuyển (GIAO_HANG_NHANH, LIEN_TINH, NOI_BO)
        String trackingCode,  // mã vận đơn
        String shipperName,   // tên shipper
        String shipperPhone,  // SĐT shipper
        String notes          // ghi chú vận chuyển (tùy chọn)
) {}
