package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Online đơn hàng tạo bởi BUYER — tách biệt khỏi Invoice POS nội bộ.
 */
@Entity
@Table(name = "buyer_orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID của BUYER tạo đơn (owner-check) */
    @Column(nullable = false)
    private Long buyerId;

    /** Tên khách hàng (snapshot) */
    private String buyerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_APPROVAL;

    private String shippingAddress;

    /** Phương thức thanh toán (COD, ONLINE, BANK_TRANSFER) */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    /** Trạng thái thanh toán */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private String notes;

    /** Coupon code áp dụng */
    private String couponCode;

    /** Tổng tạm tính (trước giảm giá) */
    private BigDecimal subtotal;

    /** Giảm giá */
    private BigDecimal discount;

    /** Tổng thanh toán cuối */
    private BigDecimal total;

    /** Lý do từ chối (khi REJECTED) */
    private String rejectionReason;

    /** PHARMACIST xử lý đơn */
    private Long processedBy;

    /** Thông tin vận chuyển */
    private String carrier;        // đơn vị vận chuyển
    private String trackingCode;   // mã vận đơn
    private String shipperName;    // tên shipper
    private String shipperPhone;   // SĐT shipper
    private LocalDateTime shippedAt;   // thời điểm giao vận
    private LocalDateTime deliveredAt; // thời điểm giao thành công

    /** Thông tin hoàn trả */
    private LocalDateTime returnedAt;  // thời điểm hoàn trả
    private String returnReason;       // lý do hoàn trả

    /** Stripe PaymentIntent ID (card payment) */
    @Column(name = "stripe_payment_intent_id", length = 200)
    private String stripePaymentIntentId;



    /** Mã giao dịch từ cổng thanh toán (sau webhook) */
    @Column(name = "payment_transaction_id", length = 200)
    private String paymentTransactionId;

    /** Buyer xác nhận đã nhận hàng */
    @Builder.Default
    private Boolean buyerConfirmed = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BuyerOrderItem> items = new ArrayList<>();

    @PreUpdate
    void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
