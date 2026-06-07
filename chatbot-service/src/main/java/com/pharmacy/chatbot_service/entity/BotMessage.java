package com.pharmacy.chatbot_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Lưu từng tin nhắn trong session chatbot
 */
@Entity
@Table(name = "bot_messages")
@Getter
@Setter
public class BotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /**
     * USER hoặc BOT
     */
    @Column(nullable = false, length = 10)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Intent được nhận diện (chỉ cho role=USER)
     */
    @Column(length = 50)
    private String intent;

    /**
     * Độ tin cậy của intent matching (0.0 - 1.0)
     */
    @Column(name = "confidence")
    private Double confidence;

    /**
     * FAQ ID được dùng để trả lời (nếu có)
     */
    @Column(name = "faq_id")
    private Long faqId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
