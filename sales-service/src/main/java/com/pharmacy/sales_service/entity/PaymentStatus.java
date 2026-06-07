package com.pharmacy.sales_service.entity;

/**
 * Trạng thái thanh toán của đơn hàng.
 */
public enum PaymentStatus {
    UNPAID,    // Chưa thanh toán (mặc định khi COD)
    PAID,      // Đã thanh toán (Online lúc tạo, COD khi giao thành công)
    REFUNDED   // Đã hoàn tiền (khi RETURNED)
}
