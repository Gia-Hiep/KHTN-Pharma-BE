package com.pharmacy.chat_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Getter
@Setter
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Loại cuộc trò chuyện:
     * - USER_TO_ADMIN: Khách hàng chat với admin/pharmacist
     * - PHARMACY_TO_PHARMACY: Nhà thuốc chat với nhà thuốc
     * - SUPPORT: Chat hỗ trợ kỹ thuật
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * ID người tham gia 1 (thường là người khởi tạo)
     */
    @Column(name = "participant_1", nullable = false)
    private Long participant1;

    /**
     * ID người tham gia 2
     */
    @Column(name = "participant_2", nullable = false)
    private Long participant2;

    /**
     * Tiêu đề cuộc trò chuyện (optional)
     */
    @Column(length = 255)
    private String title;

    /**
     * Trạng thái: ACTIVE, ARCHIVED, CLOSED
     */
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
