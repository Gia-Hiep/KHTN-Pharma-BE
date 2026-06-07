package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "buyer_order_items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private BuyerOrder order;

    @Column(nullable = false)
    private Long medicineId;

    /** Tên thuốc lúc đặt hàng (snapshot, không join catalog) */
    private String medicineName;

    /** Số lượng hiện tại (có thể giảm do partial fulfillment) */
    @Column(nullable = false)
    private Integer qty;

    /** Số lượng ban đầu (trước khi báo thiếu — snapshot) */
    private Integer originalQty;

    /** Đơn giá tại thời điểm đặt */
    private BigDecimal unitPrice;

    /** Thành tiền = qty * unitPrice */
    private BigDecimal lineTotal;

    /** Giá tier: RETAIL / WHOLESALE / INSURANCE */
    private String priceTier;

    /** Snapshot đơn vị bán */
    @Column(name = "unit_code")
    private String unitCode;

    @Column(name = "unit_label")
    private String unitLabel;

    @Column(name = "conversion_factor", nullable = false)
    @Builder.Default
    private Integer conversionFactor = 1;

    /** Đánh dấu đã soạn xong (bước PICKING) */
    @Builder.Default
    private Boolean fulfilled = false;
}
