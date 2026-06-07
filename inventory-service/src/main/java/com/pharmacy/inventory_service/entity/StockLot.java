package com.pharmacy.inventory_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_lots", uniqueConstraints = @UniqueConstraint(name = "uk_lot", columnNames = {"medicine_id","lot_number","expiry_date"}))
@Getter @Setter
public class StockLot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(name = "medicine_name")
    private String medicineName;

    @Column(name = "lot_number", nullable = false)
    private String lotNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    /**
     * Import price per base unit stored in inventory.
     * Example: 1 HOP = 120000 and factor = 100 -> importPrice = 1200 on stock lot.
     */
    @Column(name = "import_price", nullable = false)
    private java.math.BigDecimal importPrice;

    @Column(name = "qty_on_hand", nullable = false)
    private int qtyOnHand;

    @Column(name = "qty_reserved", nullable = false)
    private int qtyReserved;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
