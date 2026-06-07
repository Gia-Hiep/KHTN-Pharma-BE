package com.pharmacy.chat_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /**
     * Nội dung tin nhắn
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Loại tin nhắn: TEXT, IMAGE, FILE, SYSTEM
     */
    @Column(name = "message_type", nullable = false, length = 20)
    private String messageType = "TEXT";

    /**
     * URL file đính kèm (nếu có)
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /**
     * Tên file gốc (nếu có)
     */
    @Column(name = "attachment_name", length = 255)
    private String attachmentName;

    /**
     * Thời gian đọc tin nhắn
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Đã xóa chưa (soft delete)
     */
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (messageType == null) messageType = "TEXT";
        if (isDeleted == null) isDeleted = false;
    }
}
