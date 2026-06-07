package com.pharmacy.prescription_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="prescriptions")
@Getter @Setter
public class Prescription {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="prescription_code", nullable=false, unique=true)
    private String prescriptionCode;

    @Column(name="customer_id")
    private Long customerId;

    @Column(name="customer_phone")
    private String customerPhone;

    @Column(name="doctor_name")
    private String doctorName;

    private String diagnosis;

    @Column(name="prescription_date")
    private LocalDate prescriptionDate;

    @Column(nullable=false)
    private String status;

    private String notes;

    @Column(name="created_by")
    private Long createdBy;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;
}

