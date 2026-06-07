package com.pharmacy.purchase_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="purchases")
@Getter @Setter
public class Purchase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String code;

    @Column(name="supplier_id")
    private Long supplierId;

    @Column(name="created_by", nullable=false)
    private Long createdBy;

    @Column(nullable=false)
    private String status; // DRAFT/RECEIVED/CANCELLED

    @Column(nullable=false)
    private BigDecimal total;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="received_at")
    private LocalDateTime receivedAt;

    private String notes;
}
