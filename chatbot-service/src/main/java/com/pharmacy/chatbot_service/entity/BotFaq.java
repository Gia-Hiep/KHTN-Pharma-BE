package com.pharmacy.chatbot_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Bảng câu hỏi thường gặp (FAQ) của chatbot.
 * Admin có thể thêm/sửa/xóa FAQ để huấn luyện chatbot.
 */
@Entity
@Table(name = "bot_faqs")
@Getter
@Setter
public class BotFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Intent (chủ đề) của câu hỏi:
     * MEDICINE_SEARCH, ORDER_STATUS, STORE_INFO, SIDE_EFFECTS, DOSAGE, GENERAL
     */
    @Column(nullable = false, length = 50)
    private String intent;

    /**
     * Từ khóa trigger (phân cách bằng dấu phẩy): "đau đầu,nhức đầu,headache"
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String keywords;

    /**
     * Câu hỏi mẫu (để admin hiểu nội dung)
     */
    @Column(nullable = false, length = 500)
    private String question;

    /**
     * Câu trả lời của chatbot
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    /**
     * Độ ưu tiên (cao hơn = khớp trước): 1-10
     */
    @Column(nullable = false)
    private Integer priority = 5;

    /**
     * Có active không
     */
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
