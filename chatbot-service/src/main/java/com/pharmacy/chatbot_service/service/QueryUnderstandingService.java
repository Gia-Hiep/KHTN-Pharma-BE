package com.pharmacy.chatbot_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Uses Gemini only for query understanding. It does not answer the user.
 * If Gemini fails or returns invalid JSON, the orchestrator's deterministic
 * rule result remains the source of truth.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QueryUnderstandingService {

    private static final Set<String> ALLOWED_INTENTS = Set.of(
            "ORDER_INQUIRY",
            "PRODUCT_SEARCH",
            "FAQ_POLICY",
            "MEDICAL_UNSAFE",
            "OUT_OF_SCOPE",
            "FAQ_FALLBACK"
    );

    private static final Set<String> ALLOWED_SEARCH_MODES = Set.of(
            "NAME",
            "INGREDIENT",
            "DISEASE_GROUP",
            "CATEGORY",
            "GENERAL"
    );

    private static final String SYSTEM_PROMPT = """
            Ban la bo phan hieu cau hoi cho chatbot nha thuoc.
            Chi tra ve JSON hop le, khong giai thich, khong markdown.

            Nhiem vu:
            - Phan loai intent
            - Rut gon truy van tim kiem san pham
            - Trich entity ngan gon

            Intent hop le:
            - ORDER_INQUIRY: tra cuu don hang, ma don, trang thai don
            - PRODUCT_SEARCH: tim san pham theo ten, hoat chat, danh muc, nhom benh
            - FAQ_POLICY: hoi chinh sach, giao hang, doi tra, thanh toan, huong dan
            - MEDICAL_UNSAFE: hoi lieu dung, ke don, chan doan, phu hop ca nhan
            - OUT_OF_SCOPE: ngoai pham vi nha thuoc

            Search mode hop le:
            - NAME
            - INGREDIENT
            - DISEASE_GROUP
            - CATEGORY
            - GENERAL

            Quy tac an toan:
            - "benh cam cum", "san pham cam cum", "nhom benh ho hap" la PRODUCT_SEARCH voi DISEASE_GROUP
            - "nen dung thuoc nao", "uong bao nhieu", "tre em dung duoc khong" la MEDICAL_UNSAFE
            - Khong tu van y te, khong chon thuoc thay nguoi dung

            JSON shape:
            {
              "intent": "PRODUCT_SEARCH",
              "searchMode": "DISEASE_GROUP",
              "normalizedQuery": "cam cum",
              "entities": ["cam cum"],
              "needsPharmacist": false
            }
            """;

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QueryUnderstanding understand(String message, String fallbackIntent, String fallbackQuery) {
        QueryUnderstanding fallback = QueryUnderstanding.fallback(fallbackIntent, fallbackQuery);
        if (message == null || message.isBlank()) {
            return fallback;
        }

        String userPrompt = """
                Cau hoi nguoi dung: %s
                Fallback intent bang rule: %s
                Fallback query bang rule: %s

                Hay tra ve JSON duy nhat theo schema da yeu cau.
                """.formatted(message, fallbackIntent, fallbackQuery);

        String raw = geminiService.generate(SYSTEM_PROMPT, userPrompt);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }

        try {
            JsonNode root = objectMapper.readTree(extractJson(raw));
            String intent = normalizeEnum(root.path("intent").asText(null));
            if (!ALLOWED_INTENTS.contains(intent)) {
                return fallback;
            }

            String searchMode = normalizeEnum(root.path("searchMode").asText("GENERAL"));
            if (!ALLOWED_SEARCH_MODES.contains(searchMode)) {
                searchMode = "GENERAL";
            }

            String normalizedQuery = normalizeSearchQuery(root.path("normalizedQuery").asText(null));
            if (normalizedQuery.isBlank()) {
                normalizedQuery = normalizeSearchQuery(fallbackQuery);
            }

            List<String> entities = new ArrayList<>();
            JsonNode entityNode = root.path("entities");
            if (entityNode.isArray()) {
                for (JsonNode item : entityNode) {
                    String entity = normalizeSearchQuery(item.asText(null));
                    if (!entity.isBlank() && !entities.contains(entity)) {
                        entities.add(entity);
                    }
                }
            }
            if (entities.isEmpty() && !normalizedQuery.isBlank()) {
                entities.add(normalizedQuery);
            }

            return new QueryUnderstanding(
                    intent,
                    searchMode,
                    normalizedQuery,
                    List.copyOf(entities),
                    root.path("needsPharmacist").asBoolean(false),
                    "GEMINI"
            );
        } catch (Exception e) {
            log.warn("Query understanding parse failed, fallback to rules: {}", e.getMessage());
            return fallback;
        }
    }

    private String extractJson(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("(?s)^```[a-zA-Z]*\\s*", "")
                    .replaceFirst("(?s)\\s*```$", "")
                    .trim();
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private String normalizeEnum(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeSearchQuery(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text.toLowerCase(Locale.ROOT).trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('\u0111', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public record QueryUnderstanding(
            String intent,
            String searchMode,
            String normalizedQuery,
            List<String> entities,
            boolean needsPharmacist,
            String source
    ) {
        public static QueryUnderstanding fallback(String intent, String query) {
            String resolvedIntent = intent == null || intent.isBlank() ? "FAQ_FALLBACK" : intent;
            String resolvedQuery = query == null ? "" : query;
            return new QueryUnderstanding(
                    resolvedIntent,
                    "GENERAL",
                    resolvedQuery,
                    resolvedQuery.isBlank() ? List.of() : List.of(resolvedQuery),
                    false,
                    "RULE"
            );
        }
    }
}
