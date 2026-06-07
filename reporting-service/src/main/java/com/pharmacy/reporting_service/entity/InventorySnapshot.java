package com.pharmacy.reporting_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Snapshot tồn kho theo ngày — chụp trạng thái cuối ngày
 */
@Entity
@Table(name = "inventory_snapshots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"snapshot_date", "medicine_id"}))
@Getter
@Setter
public class InventorySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(name = "medicine_name", length = 255)
    private String medicineName;

    @Column(name = "quantity_on_hand")
    private Integer quantityOnHand = 0;

    @Column(name = "quantity_sold_today")
    private Integer quantitySoldToday = 0;

    @Column(name = "quantity_received_today")
    private Integer quantityReceivedToday = 0;

    /** Giá trị tồn kho tại thời điểm chụp */
    @Column(name = "stock_value", precision = 18, scale = 2)
    private BigDecimal stockValue = BigDecimal.ZERO;

    /** LOW_STOCK, OUT_OF_STOCK, NORMAL, EXPIRING_SOON */
    @Column(name = "stock_status", length = 20)
    private String stockStatus = "NORMAL";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
