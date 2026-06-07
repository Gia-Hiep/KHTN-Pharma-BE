package com.pharmacy.chatbot_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * User feedback sau cuộc trò chuyện với chatbot
 */
@Entity
@Table(name = "bot_feedbacks")
@Getter
@Setter
public class BotFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "user_id")
    private Long userId;

    /**
     * Điểm đánh giá: 1-5
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * Nhận xét tự do
     */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /**
     * Chatbot có giải quyết được vấn đề không?
     */
    @Column(name = "resolved")
    private Boolean resolved;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
