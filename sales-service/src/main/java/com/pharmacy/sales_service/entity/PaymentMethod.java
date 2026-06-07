package com.pharmacy.sales_service.entity;

/**
 * Phương thức thanh toán.
 */
public enum PaymentMethod {
    COD,            // Thanh toán khi nhận hàng
    CASH,           // Alias cũ cho COD — backward compatibility với DB
    ONLINE,         // Thanh toán online (VNPAY/MOMO/etc)
    BANK_TRANSFER,  // Chuyển khoản ngân hàng
    STRIPE,         // Thẻ quốc tế qua Stripe
    WALLET          // Thanh toán bằng ví
}
