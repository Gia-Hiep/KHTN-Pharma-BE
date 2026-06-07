package com.pharmacy.chatbot_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Guardrail y tế: chặn MEDICAL_UNSAFE bằng keyword + regex.
 * Deterministic — không dùng Gemini. Ưu tiên cao nhất.
 */
@Service
@Slf4j
public class GuardrailService {

    private static final Set<String> BLOCKED_FAQ_INTENTS = Set.of(
            "DOSAGE",
            "PRESCRIPTION",
            "SIDE_EFFECTS"
    );

    public record GuardrailResult(boolean safe, String method, String matchedPattern) {
        public static GuardrailResult passed() { return new GuardrailResult(true, null, null); }
        public static GuardrailResult blocked(String method, String pattern) {
            return new GuardrailResult(false, method, pattern);
        }
    }

    // ═══ LAYER 1: Keyword match ═══
    private static final List<String> UNSAFE_KEYWORDS = List.of(
            // Liều dùng
            "liều dùng", "lieu dung", "uống bao nhiêu", "uong bao nhieu",
            "mấy viên", "may vien", "bao nhiêu viên", "bao nhieu vien",
            "dùng bao nhiêu", "dung bao nhieu", "mấy lần một ngày",
            "liều cho", "lieu cho",
            // Kê đơn
            "kê đơn", "ke don", "kê thuốc", "ke thuoc", "viết đơn thuốc",
            "kê cho tôi", "ke cho toi",
            // Chẩn đoán
            "chẩn đoán", "chan doan", "tôi bị bệnh gì", "toi bi benh gi",
            // Tư vấn cá nhân
            "có hợp với tôi", "co hop voi toi",
            "nên dùng thuốc nào", "nen dung thuoc nao",
            "nên uống thuốc nào", "nen uong thuoc nao",
            "thuốc nào để trị", "thuoc nao de tri",
            "thay thuốc", "thay thế thuốc", "phối hợp thuốc",
            // Đối tượng đặc biệt
            "trẻ em dùng được không", "tre em dung duoc khong",
            "phụ nữ có thai", "phu nu co thai",
            "bà bầu dùng", "ba bau dung",
            "cho con bú", "cho con bu",
            "người già dùng", "nguoi gia dung",
            "người bệnh nền", "nguoi benh nen",
            "tiểu đường dùng", "tieu duong dung"
    );

    // ═══ LAYER 2: Regex patterns ═══
    private static final List<Pattern> UNSAFE_PATTERNS = List.of(
            // "thuốc X uống bao nhiêu" / "uống X mấy viên"
            Pattern.compile("(?i)(thuốc|uống|dùng).{0,20}(bao nhiêu|mấy viên|mấy lần|liều)"),
            // "trẻ em / bà bầu / người già + dùng/uống"
            Pattern.compile("(?i)(trẻ em|bà bầu|phụ nữ có thai|cho con bú|người già|người cao tuổi|người bệnh).{0,15}(dùng|uống|được không|có thể)"),
            // "nên dùng thuốc nào để trị X"
            Pattern.compile("(?i)(nên|hãy|cho tôi).{0,15}(thuốc|đơn thuốc|kê).{0,15}(trị|chữa|điều trị)"),
            // "thuốc X có hợp với"
            Pattern.compile("(?i)(thuốc|sản phẩm).{0,20}(hợp với|phù hợp|an toàn cho)")
    );

    private static final String REFUSAL_MESSAGE =
            "Xin lỗi, chatbot không thể tư vấn liều dùng, kê đơn hoặc đưa lời khuyên y tế cá nhân. " +
            "Bạn vui lòng liên hệ dược sĩ để được hỗ trợ chính xác hơn. 💊\n\n" +
            "Tôi có thể giúp bạn:\n• Tra cứu đơn hàng\n• Tìm sản phẩm\n• Giải đáp chính sách";

    /**
     * Kiểm tra an toàn y tế. Gọi TRƯỚC khi phân loại intent.
     */
    public GuardrailResult check(String message) {
        String normalized = normalize(message);

        // Layer 1: Keyword
        for (String kw : UNSAFE_KEYWORDS) {
            if (normalized.contains(normalize(kw))) {
                log.info("MEDICAL_UNSAFE detected by keyword: '{}'", kw);
                return GuardrailResult.blocked("keyword_match", kw);
            }
        }

        // Layer 2: Regex
        for (Pattern p : UNSAFE_PATTERNS) {
            if (p.matcher(message).find()) {
                log.info("MEDICAL_UNSAFE detected by regex: '{}'", p.pattern());
                return GuardrailResult.blocked("regex_match", p.pattern());
            }
        }

        return GuardrailResult.passed();
    }

    public String getRefusalMessage() {
        return REFUSAL_MESSAGE;
    }

    public GuardrailResult checkFaqContent(String intent, String keywords, String question, String answer) {
        if (intent != null && BLOCKED_FAQ_INTENTS.contains(intent.trim().toUpperCase())) {
            return GuardrailResult.blocked("faq_intent", intent.trim().toUpperCase());
        }

        String combined = String.join("\n",
                intent == null ? "" : intent,
                keywords == null ? "" : keywords,
                question == null ? "" : question,
                answer == null ? "" : answer
        ).trim();

        if (combined.isBlank()) {
            return GuardrailResult.passed();
        }
        return check(combined);
    }

    public boolean isBlockedFaqIntent(String intent) {
        return intent != null && BLOCKED_FAQ_INTENTS.contains(intent.trim().toUpperCase());
    }

    public GuardrailResult checkDocumentContent(String sourceType, String title, String content) {
        String combined = String.join("\n",
                sourceType == null ? "" : sourceType,
                title == null ? "" : title,
                content == null ? "" : content
        ).trim();

        if (combined.isBlank()) {
            return GuardrailResult.passed();
        }
        return check(combined);
    }

    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase().trim()
                .replaceAll("[^a-z0-9àáảãạăắặẳẵâấầẩẫậđèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
