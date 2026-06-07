package com.pharmacy.catalog_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bảng giá theo tier khách hàng (giá sỉ, giá lẻ, giá VIP, giá khuyến mãi)
 */
@Entity
@Table(name = "medicine_pricing_tiers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pricing_tier",
                columnNames = {"medicine_id", "tier_code", "effective_from"}
        ))
@Getter
@Setter
public class MedicinePricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    /**
     * Loại khách hàng: RETAIL (khách lẻ), WHOLESALE (nhà thuốc sỉ), VIP, GOLD, SILVER
     */
    @Column(name = "tier_code", nullable = false, length = 50)
    private String tierCode;

    /**
     * Số lượng tối thiểu để áp dụng giá này
     */
    @Column(name = "min_qty", nullable = false)
    private Integer minQty = 1;

    /**
     * Giá cho tier này
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /**
     * Phần trăm chiết khấu (0-100)
     */
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    /**
     * Ngày bắt đầu hiệu lực
     */
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    /**
     * Ngày hết hiệu lực (null = vô thời hạn)
     */
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (minQty == null) minQty = 1;
        if (discountPercent == null) discountPercent = BigDecimal.ZERO;
    }
}
