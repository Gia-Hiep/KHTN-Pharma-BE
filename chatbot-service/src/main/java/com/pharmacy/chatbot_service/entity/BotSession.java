package com.pharmacy.chatbot_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Lưu lịch sử cuộc trò chuyện với chatbot
 */
@Entity
@Table(name = "bot_sessions")
@Getter
@Setter
public class BotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID người dùng (null nếu là khách vãng lai)
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Session token cho anonymous users
     */
    @Column(name = "session_token", length = 100)
    private String sessionToken;

    /**
     * Trạng thái: ACTIVE, ENDED, ESCALATED (escalated = chuyển sang human)
     */
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    /**
     * Ngữ cảnh hiện tại (intent đang xử lý)
     */
    @Column(name = "current_intent", length = 50)
    private String currentIntent;

    /**
     * Số lần chatbot không hiểu (để quyết định escalate)
     */
    @Column(name = "fallback_count")
    private Integer fallbackCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
    }
}
