package com.pharmacy.catalog_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="medicine_price_history")
@Getter @Setter
public class MedicinePriceHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="medicine_id", nullable=false)
    private Long medicineId;

    @Column(name="old_price", nullable=false)
    private BigDecimal oldPrice;

    @Column(name="new_price", nullable=false)
    private BigDecimal newPrice;

    @Column(name="changed_by")
    private Long changedBy;

    @Column(name="changed_at", nullable=false)
    private LocalDateTime changedAt;
}
