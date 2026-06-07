package com.pharmacy.catalog_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="medicines")
@Getter @Setter
public class Medicine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String code;

    @Column(nullable=false)
    private String name;

    @Column(name="generic_name")
    private String genericName;

    /**
     * Base unit label kept for backward compatibility.
     * Detailed sale units are modeled in medicine_units.
     */
    @Column(nullable=false)
    private String unit;

    @Column(name="is_rx", nullable=false)
    private boolean isRx;

    private String manufacturer;

    /** Hoạt chất (ví dụ: Paracetamol, Amoxicillin) */
    @Column(name="active_ingredient")
    private String activeIngredient;

    /** Dạng bào chế (viên nén, si-rô, ống tiêm...) */
    @Column(name="dosage_form")
    private String dosageForm;

    /** Quy cách đóng gói (hộp 10 vỉ x 10 viên...) */
    @Column(name="package_size")
    private String packageSize;

    /** Xuất xứ (Việt Nam, Hàn Quốc...) */
    private String origin;

    @Column(name="category_id")
    private Long categoryId;

    @Column(name="default_supplier_id")
    private Long defaultSupplierId;

    /**
     * Base-unit retail price kept for backward compatibility.
     */
    @Column(name="sale_price", nullable=false)
    private BigDecimal salePrice;

    private String barcode;

    @Column(name="image_url")
    private String imageUrl;

    private String description;

    @Column(name="usage_instructions")
    private String usageInstructions;

    @Column(name="side_effects")
    private String sideEffects;

    @Column(nullable=false)
    private String status; // ACTIVE/INACTIVE

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    @Transient
    private List<MedicineUnit> units;
}
