package com.pharmacy.prescription_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="prescription_items")
@Getter @Setter
public class PrescriptionItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="prescription_id", nullable=false)
    private Long prescriptionId;

    @Column(name="medicine_id", nullable=false)
    private Long medicineId;

    @Column(name="qty" , nullable=false)
    private int quantity;

    @Column(name="dosage_instructions")
    private String dosageInstructions;
}
