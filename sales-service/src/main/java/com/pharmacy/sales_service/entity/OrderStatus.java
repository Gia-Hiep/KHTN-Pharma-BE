package com.pharmacy.sales_service.entity;

public enum OrderStatus {
    PENDING_APPROVAL,
    CONFIRMED,
    PICKING,
    PACKING,
    SHIPPING,
    DELIVERED,
    REJECTED,
    CANCELLED,
    RETURNED    // Khách trả hàng sau khi nhận
}
