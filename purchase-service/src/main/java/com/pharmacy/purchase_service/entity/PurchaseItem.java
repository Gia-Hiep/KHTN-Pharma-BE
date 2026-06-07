package com.pharmacy.purchase_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="purchase_items")
@Getter @Setter
public class PurchaseItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="purchase_id", nullable=false)
    private Long purchaseId;

    @Column(name="medicine_id", nullable=false)
    private Long medicineId;

    @Column(name="unit_code", length = 30)
    private String unitCode;

    @Column(name="unit_label", length = 80)
    private String unitLabel;

    @Column(name="conversion_factor", nullable=false)
    private Integer conversionFactor = 1;

    @Column(name="medicine_name")
    private String medicineName;

    @Column(name="lot_number", nullable=false)
    private String lotNumber;

    @Column(name="expiry_date", nullable=false)
    private LocalDate expiryDate;

    /**
     * Import price per purchase unit on the purchase document.
     * Example: 1 HOP = 120000.
     */
    @Column(name="import_price", nullable=false)
    private BigDecimal importPrice;

    @Column(nullable=false)
    private int qty;

    @Column(name="line_total", nullable=false)
    private BigDecimal lineTotal;
}
