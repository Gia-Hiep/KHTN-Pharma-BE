package com.pharmacy.inventory_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_alert_rules")
@Getter @Setter
public class StockAlertRule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="medicine_id", nullable = false, unique = true)
    private Long medicineId;

    @Column(name="min_stock_level", nullable = false)
    private int minStockLevel;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
