package com.pharmacy.customer_service.dto;

import java.math.BigDecimal;

public record ApplyCouponResponse(
        boolean valid,
        String message,
        BigDecimal discountAmount,
        String couponCode,
        String discountType
) {
    public static ApplyCouponResponse invalid(String message) {
        return new ApplyCouponResponse(false, message, BigDecimal.ZERO, null, null);
    }

    public static ApplyCouponResponse success(String code, String type, BigDecimal discount) {
        return new ApplyCouponResponse(true, "Coupon applied successfully", discount, code, type);
    }
}
