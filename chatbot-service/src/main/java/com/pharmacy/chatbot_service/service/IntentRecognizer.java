package com.pharmacy.chatbot_service.service;

import com.pharmacy.chatbot_service.entity.BotFaq;
import com.pharmacy.chatbot_service.repository.BotFaqRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Nhận diện ý định (intent) của người dùng dựa trên keyword matching.
 * Đây là rule-based approach, không cần AI/ML.
 */
@Service
@RequiredArgsConstructor
public class IntentRecognizer {

    private final BotFaqRepo faqRepo;

    /**
     * Kết quả nhận diện intent
     */
    public record IntentResult(
            String intent,
            BotFaq matchedFaq,
            double confidence
    ) {}

    /**
     * Nhận diện intent từ message của người dùng
     */
    public IntentResult recognize(String message) {
        String normalizedMsg = normalize(message);

        List<BotFaq> faqs = faqRepo.findAllActive();

        BotFaq bestFaq = null;
        double bestScore = 0.0;

        for (BotFaq faq : faqs) {
            double score = calculateScore(normalizedMsg, faq);
            if (score > bestScore) {
                bestScore = score;
                bestFaq = faq;
            }
        }

        // Ngưỡng tin cậy tối thiểu
        if (bestScore < 0.3) {
            return new IntentResult("FALLBACK", null, 0.0);
        }

        return new IntentResult(bestFaq.getIntent(), bestFaq, bestScore);
    }

    /**
     * Tính điểm khớp giữa message và FAQ
     */
    private double calculateScore(String normalizedMsg, BotFaq faq) {
        String[] keywords = faq.getKeywords().split(",");
        int matchCount = 0;

        for (String kw : keywords) {
            String normalized = normalize(kw.trim());
            if (!normalized.isEmpty() && normalizedMsg.contains(normalized)) {
                matchCount++;
            }
        }

        if (matchCount == 0) return 0.0;

        // Score = tỷ lệ keywords khớp × priority bonus
        double keywordScore = (double) matchCount / keywords.length;
        double priorityBonus = faq.getPriority() / 10.0 * 0.2; // max 0.2 bonus
        return Math.min(1.0, keywordScore + priorityBonus);
    }

    /**
     * Chuẩn hóa text: lowercase, bỏ dấu cơ bản
     */
    public String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .trim()
                // Bỏ các ký tự đặc biệt
                .replaceAll("[^a-z0-9àáảãạăắặẳẵặâấầẩẫậđèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
