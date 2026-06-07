package com.pharmacy.purchase_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="suppliers")
@Getter @Setter
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name="contact_person")
    private String contactPerson;

    private String phone;
    private String email;

    private String address;
    private String notes;

    @Column(name="created_at")
    private LocalDateTime createdAt;
}
