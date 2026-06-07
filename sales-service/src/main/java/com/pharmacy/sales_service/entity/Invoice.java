package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="invoices")
@Getter @Setter
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name="customer_id")
    private Long customerId;


    @Column(name="cashier_id", nullable = false)
    private Long cashierId;

    @Column(nullable = false)
    private String status; // DRAFT/WAIT_PAYMENT/PAID/CANCELLED

    @Column(name="payment_status", nullable = false)
    private String paymentStatus; // UNPAID/PAID/REFUNDED/DEBT

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal discount;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private String notes;

    // ===== ORDER TYPE & CHANNEL =====

    @Column(name = "order_type", length = 20)
    private String orderType = "RETAIL"; // RETAIL, WHOLESALE, ONLINE

    @Column(name = "channel", length = 20)
    private String channel = "POS"; // POS, WEB, APP

    // ===== SHIPPING =====

    @Column(name = "requires_shipping")
    private Boolean requiresShipping = false;

    @Column(name = "shipping_address", length = 1000)
    private String shippingAddress;

    @Column(name = "shipping_fee", precision = 15, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "shipping_status", length = 20)
    private String shippingStatus; // PENDING, PICKED_UP, IN_TRANSIT, DELIVERED

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    // ===== COUPON & DEBT =====

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "coupon_discount", precision = 15, scale = 2)
    private BigDecimal couponDiscount = BigDecimal.ZERO;



    @PrePersist
    protected void onCreate() {
        if (orderType == null) orderType = "RETAIL";
        if (channel == null) channel = "POS";
        if (requiresShipping == null) requiresShipping = false;
        if (shippingFee == null) shippingFee = BigDecimal.ZERO;
        if (couponDiscount == null) couponDiscount = BigDecimal.ZERO;

    }
}

