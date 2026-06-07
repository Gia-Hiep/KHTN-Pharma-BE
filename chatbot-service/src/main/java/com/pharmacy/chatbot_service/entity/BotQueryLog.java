package com.pharmacy.chatbot_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Log mỗi query chatbot: intent, route, response time, guardrail block.
 */
@Entity
@Table(name = "bot_query_logs", indexes = {
        @Index(name = "idx_qlog_user", columnList = "user_id")
})
@Getter
@Setter
public class BotQueryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "detected_intent", length = 30)
    private String detectedIntent;

    /** RAG, API, GUARDRAIL, OUT_OF_SCOPE */
    @Column(name = "route_used", length = 20)
    private String routeUsed;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "blocked_by_guardrail")
    private Boolean blockedByGuardrail = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
