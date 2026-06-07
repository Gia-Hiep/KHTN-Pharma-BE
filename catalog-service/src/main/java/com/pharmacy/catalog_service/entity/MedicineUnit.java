package com.pharmacy.catalog_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicine_units",
        uniqueConstraints = @UniqueConstraint(name = "uk_medicine_unit_code", columnNames = {"medicine_id", "unit_code"}))
@Getter
@Setter
public class MedicineUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(name = "unit_code", nullable = false, length = 30)
    private String unitCode;

    @Column(name = "unit_label", nullable = false, length = 80)
    private String unitLabel;

    @Column(name = "conversion_factor", nullable = false)
    private Integer conversionFactor = 1;

    @Column(name = "retail_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal retailPrice = BigDecimal.ZERO;

    @Column(name = "wholesale_price", precision = 15, scale = 2)
    private BigDecimal wholesalePrice;

    @Column(name = "wholesale_min_qty")
    private Integer wholesaleMinQty;

    @Column(name = "is_base_unit", nullable = false)
    private Boolean isBaseUnit = false;

    @Column(name = "is_default_sale_unit", nullable = false)
    private Boolean isDefaultSaleUnit = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (conversionFactor == null || conversionFactor < 1) conversionFactor = 1;
        if (retailPrice == null) retailPrice = BigDecimal.ZERO;
        if (isBaseUnit == null) isBaseUnit = false;
        if (isDefaultSaleUnit == null) isDefaultSaleUnit = false;
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (conversionFactor == null || conversionFactor < 1) conversionFactor = 1;
        if (retailPrice == null) retailPrice = BigDecimal.ZERO;
    }
}
