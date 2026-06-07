package com.pharmacy.reporting_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Snapshot nhập hàng theo ngày từ purchase-service
 */
@Entity
@Table(name = "purchase_snapshots",
        uniqueConstraints = @UniqueConstraint(columnNames = "report_date"))
@Getter
@Setter
public class PurchaseSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "completed_orders")
    private Integer completedOrders = 0;

    @Column(name = "pending_orders")
    private Integer pendingOrders = 0;

    @Column(name = "total_purchase_value", precision = 18, scale = 2)
    private BigDecimal totalPurchaseValue = BigDecimal.ZERO;

    @Column(name = "total_items_received")
    private Integer totalItemsReceived = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
