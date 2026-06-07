package com.pharmacy.sales_service.dto;

/**
 * Bước 5→6: Shipper báo kết quả giao hàng.
 */
public record DeliveryResultRequest(
        boolean success,          // true = DELIVERED, false = REJECTED (giao thất bại)
        String failureReason      // lý do thất bại (nếu success=false)
) {}
