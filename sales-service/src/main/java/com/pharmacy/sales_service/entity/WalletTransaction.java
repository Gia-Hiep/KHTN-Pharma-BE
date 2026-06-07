package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lịch sử giao dịch ví — mỗi khi ví thay đổi số dư.
 */
@Entity
@Table(name = "wallet_transactions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long buyerId;

    /** Loại giao dịch */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /** Số tiền (luôn dương) */
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal amount;

    /** Số dư sau giao dịch */
    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal balanceAfter;

    /** Đơn hàng liên quan (nullable) */
    private Long orderId;

    /** Mô tả giao dịch */
    private String description;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {
        REFUND,     // Hoàn tiền từ đơn hủy
        PAYMENT,    // Thanh toán đơn hàng
        TOP_UP      // Nạp tiền (dự phòng)
    }
}
