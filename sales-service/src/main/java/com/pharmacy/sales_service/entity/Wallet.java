package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ví điện tử của Buyer — lưu số dư để hoàn tiền & thanh toán.
 */
@Entity
@Table(name = "wallets")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mỗi buyer chỉ có 1 ví */
    @Column(nullable = false, unique = true)
    private Long buyerId;

    /** Số dư hiện tại (VND) */
    @Column(nullable = false, precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
