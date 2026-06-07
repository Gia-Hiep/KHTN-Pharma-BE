package com.pharmacy.chatbot_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Chunk của document đã được chia nhỏ + embedding vector (JSON).
 * Embedding lưu dạng JSON array: [0.12, -0.45, ...] (768 floats).
 */
@Entity
@Table(name = "bot_document_chunks", indexes = {
        @Index(name = "idx_chunk_doc", columnList = "document_id"),
        @Index(name = "idx_chunk_faq", columnList = "faq_id")
})
@Getter
@Setter
public class BotDocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "faq_id")
    private Long faqId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "source_type", length = 30)
    private String sourceType;

    @Column(name = "source_title", length = 255)
    private String sourceTitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Embedding vector stored as JSON array: [0.12, -0.45, ...] */
    @Column(columnDefinition = "JSON")
    private String embedding;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
