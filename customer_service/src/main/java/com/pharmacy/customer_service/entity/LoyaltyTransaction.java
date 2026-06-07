package com.pharmacy.customer_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Lịch sử giao dịch điểm thưởng (loyalty points)
 */
@Entity
@Table(name = "loyalty_transactions",
        indexes = {
                @Index(name = "idx_loyalty_customer", columnList = "customer_id"),
                @Index(name = "idx_loyalty_type", columnList = "type")
        })
@Getter
@Setter
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Loại giao dịch: EARN (tích điểm), REDEEM (đổi điểm), EXPIRE (hết hạn), ADJUST (điều chỉnh)
     */
    @Column(nullable = false, length = 20)
    private String type;

    /**
     * Số điểm (dương = cộng, âm = trừ)
     */
    @Column(nullable = false)
    private Integer points;

    /**
     * Loại tham chiếu: INVOICE, PROMOTION, MANUAL, COUPON
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * ID/code tham chiếu
     */
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
