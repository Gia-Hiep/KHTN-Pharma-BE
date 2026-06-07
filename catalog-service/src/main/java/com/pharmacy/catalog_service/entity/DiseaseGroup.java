package com.pharmacy.catalog_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Nhóm bệnh để phân loại thuốc (VD: Tim mạch, Tiêu hóa, Hô hấp...)
 * Dùng cho chatbot tìm kiếm thuốc theo nhóm bệnh
 */
@Entity
@Table(name = "disease_groups")
@Getter
@Setter
public class DiseaseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Keywords để chatbot match (comma-separated)
     * VD: "đau bụng,tiêu chảy,táo bón"
     */
    @Column(columnDefinition = "TEXT")
    private String keywords;
}
