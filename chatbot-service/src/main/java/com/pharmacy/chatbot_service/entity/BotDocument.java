package com.pharmacy.chatbot_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tài liệu gốc được nhập vào hệ thống RAG.
 * Nguồn: FAQ, chính sách, hướng dẫn sử dụng.
 */
@Entity
@Table(name = "bot_documents")
@Getter
@Setter
public class BotDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    /** FAQ, POLICY, GUIDE */
    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
