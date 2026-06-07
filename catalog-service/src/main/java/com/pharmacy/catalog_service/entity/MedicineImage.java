package com.pharmacy.catalog_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medicine_images", indexes = {
        @Index(name = "idx_mi_medicine_id", columnList = "medicine_id"),
        @Index(name = "idx_mi_medicine_sort", columnList = "medicine_id, sort_order")
})
@Getter
@Setter
public class MedicineImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
