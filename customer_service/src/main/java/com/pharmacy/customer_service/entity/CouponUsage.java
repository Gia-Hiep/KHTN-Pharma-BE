package com.pharmacy.customer_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lịch sử sử dụng coupon
 */
@Entity
@Table(name = "coupon_usage",
        indexes = {
                @Index(name = "idx_usage_coupon", columnList = "coupon_id"),
                @Index(name = "idx_usage_customer", columnList = "customer_id")
        })
@Getter
@Setter
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Invoice ID từ sales-service
     */
    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    /**
     * Số tiền được giảm
     */
    @Column(name = "discount_applied", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountApplied;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        usedAt = LocalDateTime.now();
    }
}
