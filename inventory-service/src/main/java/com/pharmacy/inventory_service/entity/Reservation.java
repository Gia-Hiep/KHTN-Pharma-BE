package com.pharmacy.inventory_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations",
        uniqueConstraints = @UniqueConstraint(name = "uk_res_ref", columnNames = {"ref_type","ref_id"}))
@Getter @Setter
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_type", nullable = false)
    private String refType; // INVOICE

    @Column(name = "ref_id", nullable = false)
    private String refId; // invoice_code

    @Column(nullable = false)
    private String status; // ACTIVE/COMMITTED/RELEASED

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
