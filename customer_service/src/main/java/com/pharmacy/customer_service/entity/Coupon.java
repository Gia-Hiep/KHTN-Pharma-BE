package com.pharmacy.customer_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Quản lý mã giảm giá (coupons)
 */
@Entity
@Table(name = "coupons",
        indexes = {
                @Index(name = "idx_coupon_code", columnList = "code", unique = true)
        })
@Getter
@Setter
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã giảm giá unique
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Loại giảm giá: PERCENT hoặc FIXED
     */
    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType;

    /**
     * Giá trị giảm (% nếu PERCENT, số tiền nếu FIXED)
     */
    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    /**
     * Giá trị đơn hàng tối thiểu để áp dụng
     */
    @Column(name = "min_order_amount", precision = 15, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    /**
     * Giảm giá tối đa (cho PERCENT)
     */
    @Column(name = "max_discount", precision = 15, scale = 2)
    private BigDecimal maxDiscount;

    /**
     * Số lần sử dụng tối đa (null = unlimited)
     */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /**
     * Số lần đã sử dụng
     */
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    /**
     * Ngày bắt đầu hiệu lực
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    /**
     * Ngày hết hạn
     */
    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    /**
     * Tier khách hàng được áp dụng (null = all tiers)
     */
    @Column(name = "customer_tier", length = 20)
    private String customerTier;

    /**
     * Loại khách hàng được áp dụng (null = all types)
     */
    @Column(name = "customer_type", length = 20)
    private String customerType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    private String description;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (usedCount == null) usedCount = 0;
        if (isActive == null) isActive = true;
        if (minOrderAmount == null) minOrderAmount = BigDecimal.ZERO;
    }

    /**
     * Kiểm tra coupon còn hiệu lực không
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        if (!isActive) return false;
        if (now.isBefore(validFrom) || now.isAfter(validTo)) return false;
        if (usageLimit != null && usedCount >= usageLimit) return false;
        return true;
    }

    /**
     * Tính số tiền được giảm
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (orderAmount.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if ("PERCENT".equals(discountType)) {
            discount = orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
            if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }
        } else {
            discount = discountValue;
        }

        // Không giảm quá giá trị đơn hàng
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }
}
