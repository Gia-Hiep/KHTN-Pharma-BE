package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Một dòng sản phẩm trong giỏ hàng.
 * Unique constraint keeps compatibility with price_tier for now,
 * but unit_code is part of the business identity of a cart line.
 */
@Entity
@Table(name = "cart_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cart_id", "medicine_id", "price_tier", "unit_code"})
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    /** Tên thuốc snapshot — hiển thị UI không cần gọi catalog */
    private String medicineName;

    /** URL ảnh thumbnail */
    private String imageUrl;

    /** Số lượng */
    @Column(nullable = false)
    private Integer qty;

    /** Đơn giá tại thời điểm thêm vào giỏ */
    private BigDecimal unitPrice;

    /** Bảng giá: RETAIL, WHOLESALE, ... */
    @Column(name = "price_tier")
    @Builder.Default
    private String priceTier = "RETAIL";

    /** Snapshot đơn vị bán tại thời điểm thêm vào giỏ */
    @Column(name = "unit_code")
    private String unitCode;

    @Column(name = "unit_label")
    private String unitLabel;

    @Column(name = "conversion_factor", nullable = false)
    @Builder.Default
    private Integer conversionFactor = 1;
}
