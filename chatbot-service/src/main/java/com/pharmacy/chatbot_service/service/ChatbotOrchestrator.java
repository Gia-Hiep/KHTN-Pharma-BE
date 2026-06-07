package com.pharmacy.chatbot_service.service;

import com.pharmacy.chatbot_service.dto.ChatQueryResponse;
import com.pharmacy.chatbot_service.dto.ChatQueryResponse.Action;
import com.pharmacy.chatbot_service.dto.ChatQueryResponse.OrderItem;
import com.pharmacy.chatbot_service.dto.ChatQueryResponse.ProductItem;
import com.pharmacy.chatbot_service.dto.ChatQueryResponse.Source;
import com.pharmacy.chatbot_service.entity.BotQueryLog;
import com.pharmacy.chatbot_service.entity.BotSession;
import com.pharmacy.chatbot_service.repository.BotQueryLogRepo;
import com.pharmacy.chatbot_service.repository.BotSessionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Main chatbot pipeline:
 * actionId -> guardrail -> intent -> route -> respond -> log
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotOrchestrator {

    private final GuardrailService guardrailService;
    private final GeminiService geminiService;
    private final RagService ragService;
    private final ApiToolService apiToolService;
    private final QueryUnderstandingService queryUnderstandingService;
    private final CatalogRagService catalogRagService;
    private final BotQueryLogRepo queryLogRepo;
    private final BotSessionRepo sessionRepo;

    private static final Map<String, Integer> ORDER_SYNONYMS = Map.ofEntries(
            Map.entry("trang thai don", 6),
            Map.entry("chi tiet don", 6),
            Map.entry("theo doi don", 6),
            Map.entry("kiem tra don", 5),
            Map.entry("don cua toi", 5),
            Map.entry("xem don", 4),
            Map.entry("don hang", 4),
            Map.entry("danh sach don", 4),
            Map.entry("ma don", 4),
            Map.entry("order", 3),
            Map.entry("don", 1)
    );

    private static final Map<String, Integer> PRODUCT_SYNONYMS = Map.ofEntries(
            Map.entry("tim thuoc", 6),
            Map.entry("tim san pham", 6),
            Map.entry("xem thuoc", 5),
            Map.entry("gia thuoc", 5),
            Map.entry("nhom benh", 4),
            Map.entry("trieu chung", 4),
            Map.entry("hoat chat", 4),
            Map.entry("san pham", 4),
            Map.entry("thuoc", 3),
            Map.entry("benh", 2),
            Map.entry("danh muc", 3),
            Map.entry("category", 2),
            Map.entry("gia", 1),
            Map.entry("mua", 1)
    );

    private static final Map<String, Integer> FAQ_SYNONYMS = Map.ofEntries(
            Map.entry("chinh sach", 6),
            Map.entry("giao hang", 6),
            Map.entry("doi tra", 6),
            Map.entry("doi hang", 6),
            Map.entry("tra hang", 6),
            Map.entry("thanh toan", 6),
            Map.entry("van chuyen", 5),
            Map.entry("phi ship", 5),
            Map.entry("phi giao hang", 5),
            Map.entry("ship bao nhieu", 5),
            Map.entry("huong dan", 5),
            Map.entry("cach dat hang", 5),
            Map.entry("gio lam viec", 5),
            Map.entry("hotline", 4),
            Map.entry("lien he", 4),
            Map.entry("gioi thieu", 4),
            Map.entry("mo cua", 4),
            Map.entry("website", 3),
            Map.entry("khuyen mai", 3),
            Map.entry("coupon", 2),
            Map.entry("loyalty", 2),
            Map.entry("he thong", 1)
    );

    private static final List<String> GREETING_KEYWORDS = List.of(
            "xin chao", "chao", "hello", "hi", "hey", "alo", "bot oi", "chatbot"
    );

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("(?i)(?<![a-z0-9])#?(\\d{1,10})(?![a-z0-9])");
    private static final Pattern PRODUCT_ENTITY_HINT = Pattern.compile("^[a-z0-9\\-+/. ]{2,60}$");
    private static final List<String> PRODUCT_QUERY_PREFIXES = List.of(
            "cho toi xem", "cho toi tim", "tim kiem", "liet ke",
            "co nhung", "san pham cho", "thuoc cho", "nhom benh",
            "trieu chung", "gia thuoc", "danh muc", "san pham",
            "thuoc", "benh", "tim", "xem", "mua", "gia"
    );

    private static final String SYSTEM_PROMPT = """
            Ban la tro ly ao cua nha thuoc Pharmacy Online. Nhiem vu: ho tro
            khach hang tra cuu san pham, theo doi don hang, giai dap chinh sach.

            QUY TAC:
            1. KHONG tu van lieu dung, ke don, loi khuyen y te ca nhan, chan doan benh
            2. Neu context khong du -> noi ro "chua co thong tin"
            3. KHONG bia thong tin
            4. Tra loi tieng Viet, ngan gon, than thien
            5. Tra loi dua tren CONTEXT duoc cung cap, khong tu sang tao them
            """;

    private static final String GREETING_RESPONSE =
            "Xin chào! Tôi là trợ lý AI của nhà thuốc.\n\n"
                    + "Tôi có thể hỗ trợ bạn:\n"
                    + "- Tra cứu đơn hàng\n"
                    + "- Tìm sản phẩm\n"
                    + "- Giải đáp chính sách (giao hàng, đổi trả, thanh toán)\n\n"
                    + "Bạn cần giúp gì?";

    private static final String OUT_OF_SCOPE_RESPONSE =
            "Xin lỗi, câu hỏi này nằm ngoài phạm vi hỗ trợ của tôi.\n\n"
                    + "Tôi có thể giúp bạn:\n"
                    + "- Tra cứu đơn hàng\n"
                    + "- Tìm sản phẩm\n"
                    + "- Giải đáp chính sách";

    private static final List<Action> MAIN_ACTIONS = List.of(
            new Action("VIEW_ORDERS", "Xem đơn hàng"),
            new Action("TRACK_ORDER_BY_CODE", "Tra đơn theo mã"),
            new Action("SEARCH_BY_ACTIVE_INGREDIENT", "Tìm theo hoạt chất"),
            new Action("SEARCH_BY_DISEASE_GROUP", "Tìm theo nhóm bệnh"),
            new Action("POLICY_SHIPPING", "Chính sách giao hàng")
    );

    private static final List<Action> ORDER_ACTIONS = List.of(
            new Action("TRACK_ORDER_BY_CODE", "Tra đơn theo mã"),
            new Action("SEARCH_PRODUCT", "Tìm sản phẩm"),
            new Action("POLICY_SHIPPING", "Chính sách giao hàng")
    );

    private static final List<Action> PRODUCT_ACTIONS = List.of(
            new Action("SEARCH_BY_ACTIVE_INGREDIENT", "Tìm theo hoạt chất"),
            new Action("SEARCH_BY_DISEASE_GROUP", "Tìm theo nhóm bệnh"),
            new Action("SEARCH_BY_CATEGORY", "Tìm theo danh mục"),
            new Action("VIEW_ORDERS", "Xem đơn hàng")
    );

    private static final List<Action> FAQ_ACTIONS = List.of(
            new Action("SEARCH_PRODUCT", "Tìm sản phẩm"),
            new Action("VIEW_ORDERS", "Xem đơn hàng")
    );

    private static final List<Action> GUARDRAIL_ACTIONS = List.of(
            new Action("CHAT_PHARMACIST", "Chat với dược sĩ"),
            new Action("SEARCH_PRODUCT", "Tìm sản phẩm"),
            new Action("VIEW_ORDERS", "Xem đơn hàng")
    );

    @Transactional
    public ChatQueryResponse process(String message, String actionId, Long userId, String jwtToken) {
        long startTime = System.currentTimeMillis();
        BotSession session = getOrCreateSession(userId);

        if (actionId != null && !actionId.isBlank()) {
            return routeByAction(actionId, userId, session.getId(), jwtToken, startTime);
        }

        if (message == null || message.isBlank()) {
            return buildResponse("GREETING", "Xin chào", GREETING_RESPONSE,
                    "GREETING", "HIGH", true, null, null, List.of(), MAIN_ACTIONS, session.getId());
        }

        GuardrailService.GuardrailResult safety = guardrailService.check(message);
        if (!safety.safe()) {
            String refusal = guardrailService.getRefusalMessage();
            logQuery(session.getId(), userId, message, "MEDICAL_UNSAFE", "GUARDRAIL", refusal, startTime, true);
            return buildResponse("MEDICAL_REFUSAL", "Giới hạn an toàn y tế", refusal,
                    "MEDICAL_UNSAFE", "HIGH", false, null, null, List.of(), GUARDRAIL_ACTIONS, session.getId());
        }

        String normalized = normalizeQuery(message);
        if (isGreeting(normalized)) {
            logQuery(session.getId(), userId, message, "GREETING", "TEMPLATE", GREETING_RESPONSE, startTime, false);
            return buildResponse("GREETING", "Xin chào", GREETING_RESPONSE,
                    "GREETING", "HIGH", true, null, null, List.of(), MAIN_ACTIONS, session.getId());
        }

        String ruleIntent = classifyIntent(normalized, message);
        String ruleProductQuery = "PRODUCT_SEARCH".equals(ruleIntent)
                ? extractProductQuery(normalized, message)
                : normalized;
        QueryUnderstandingService.QueryUnderstanding understanding =
                queryUnderstandingService.understand(message, ruleIntent, ruleProductQuery);

        if ("MEDICAL_UNSAFE".equals(understanding.intent()) || understanding.needsPharmacist()) {
            String refusal = guardrailService.getRefusalMessage();
            logQuery(session.getId(), userId, message, "MEDICAL_UNSAFE", "QUERY_UNDERSTANDING", refusal, startTime, true);
            return buildResponse("MEDICAL_REFUSAL", "Giới hạn an toàn y tế", refusal,
                    "MEDICAL_UNSAFE", "HIGH", false, null, null, List.of(), GUARDRAIL_ACTIONS, session.getId());
        }

        String intent = resolveIntent(ruleIntent, understanding, message);
        String productQuery = "PRODUCT_SEARCH".equals(intent)
                ? resolveProductQuery(understanding, ruleProductQuery)
                : null;
        return switch (intent) {
            case "ORDER_INQUIRY" -> handleOrderInquiry(message, userId, session.getId(), jwtToken, startTime);
            case "PRODUCT_SEARCH" -> handleProductSearch(message, normalized, productQuery, userId, session.getId(), jwtToken, startTime);
            case "FAQ_POLICY" -> handleFaqPolicy(message, userId, session.getId(), startTime);
            default -> handleFaqFallback(message, userId, session.getId(), startTime);
        };
    }

    private ChatQueryResponse routeByAction(String actionId, Long userId, Long sessionId, String jwtToken, long startTime) {
        return switch (actionId) {
            case "VIEW_ORDERS" -> handleOrderInquiry("Xem đơn hàng", userId, sessionId, jwtToken, startTime);
            case "SEARCH_PRODUCT" -> buildResponse("FAQ_ANSWER", "Tìm sản phẩm",
                    "Bạn muốn tìm sản phẩm theo tên, nhóm bệnh, hoạt chất hay danh mục? Ví dụ: 'Paracetamol', 'nhóm bệnh hô hấp', 'hoạt chất paracetamol', 'danh mục thiết bị y tế'.",
                    "PRODUCT_SEARCH", "HIGH", true, null, null, List.of(),
                    PRODUCT_ACTIONS, sessionId);
            case "SEARCH_BY_ACTIVE_INGREDIENT" -> buildResponse("FAQ_ANSWER", "Tìm theo hoạt chất",
                    "Bạn có thể nhập theo mẫu: 'hoạt chất paracetamol' hoặc 'hoạt chất amoxicillin'.",
                    "PRODUCT_SEARCH", "HIGH", true, null, null, List.of(), PRODUCT_ACTIONS, sessionId);
            case "SEARCH_BY_DISEASE_GROUP" -> buildResponse("FAQ_ANSWER", "Tìm theo nhóm bệnh",
                    "Bạn có thể nhập theo mẫu: 'nhóm bệnh cảm cúm', 'nhóm bệnh hô hấp' hoặc tên nhóm bệnh cần tìm.",
                    "PRODUCT_SEARCH", "HIGH", true, null, null, List.of(), PRODUCT_ACTIONS, sessionId);
            case "SEARCH_BY_CATEGORY" -> buildResponse("FAQ_ANSWER", "Tìm theo danh mục",
                    "Bạn có thể nhập theo mẫu: 'danh mục OTC', 'danh mục TPCN' hoặc 'danh mục thiết bị y tế'.",
                    "PRODUCT_SEARCH", "HIGH", true, null, null, List.of(), PRODUCT_ACTIONS, sessionId);
            case "TRACK_ORDER_BY_CODE" -> buildResponse("FAQ_ANSWER", "Tra cứu theo mã đơn",
                    "Bạn hãy nhập mã đơn theo mẫu '#123' hoặc 'kiểm tra đơn #123'.",
                    "ORDER_INQUIRY", "HIGH", true, null, null, List.of(), ORDER_ACTIONS, sessionId);
            case "POLICY_SHIPPING" -> handleFaqPolicy("chinh sach giao hang", userId, sessionId, startTime);
            case "POLICY_RETURN" -> handleFaqPolicy("chinh sach doi tra", userId, sessionId, startTime);
            case "CHAT_PHARMACIST" -> buildResponse("FAQ_ANSWER", "Chat với dược sĩ",
                    "Bạn có thể chat trực tiếp với dược sĩ qua mục Hỗ trợ trực tuyến trên website.",
                    "FAQ_POLICY", "HIGH", true, null, null, List.of(), MAIN_ACTIONS, sessionId);
            default -> buildResponse("OUT_OF_SCOPE", "Không rõ hành động", OUT_OF_SCOPE_RESPONSE,
                    "OUT_OF_SCOPE", "LOW", true, null, null, List.of(), MAIN_ACTIONS, sessionId);
        };
    }

    private boolean isGreeting(String normalized) {
        return GREETING_KEYWORDS.stream().anyMatch(keyword -> containsPhrase(normalized, keyword) || normalized.equals(keyword));
    }

    private String classifyIntent(String normalized, String original) {
        int orderScore = scoreMatches(normalized, ORDER_SYNONYMS);
        int productScore = scoreMatches(normalized, PRODUCT_SYNONYMS);
        int faqScore = scoreMatches(normalized, FAQ_SYNONYMS);

        Long orderId = extractOrderId(original);
        if (orderId != null) {
            orderScore += 5;
        }

        if (looksLikeProductQuery(normalized) && productScore == 0 && faqScore == 0) {
            productScore += 3;
        }

        if (orderScore > 0 && orderScore >= productScore && orderScore >= faqScore) {
            return "ORDER_INQUIRY";
        }
        if (productScore > 0 && productScore >= faqScore) {
            return "PRODUCT_SEARCH";
        }
        if (faqScore > 0) {
            return "FAQ_POLICY";
        }
        return "FAQ_FALLBACK";
    }

    private String resolveIntent(
            String ruleIntent,
            QueryUnderstandingService.QueryUnderstanding understanding,
            String originalMessage
    ) {
        if (extractOrderId(originalMessage) != null) {
            return "ORDER_INQUIRY";
        }
        if (understanding == null || understanding.intent() == null || understanding.intent().isBlank()) {
            return ruleIntent;
        }
        if ("FAQ_FALLBACK".equals(understanding.intent())) {
            return ruleIntent;
        }
        return understanding.intent();
    }

    private String resolveProductQuery(
            QueryUnderstandingService.QueryUnderstanding understanding,
            String fallbackProductQuery
    ) {
        if (understanding == null || understanding.normalizedQuery() == null || understanding.normalizedQuery().isBlank()) {
            return fallbackProductQuery;
        }
        return understanding.normalizedQuery();
    }

    private int scoreMatches(String text, Map<String, Integer> synonyms) {
        int score = 0;
        for (Map.Entry<String, Integer> entry : synonyms.entrySet()) {
            if (containsPhrase(text, entry.getKey())) {
                score += entry.getValue();
            }
        }
        return score;
    }

    private ChatQueryResponse handleOrderInquiry(String message, Long userId, Long sessionId, String jwtToken, long startTime) {
        if (jwtToken == null) {
            logQuery(sessionId, userId, message, "ORDER_INQUIRY", "API", "Can dang nhap", startTime, false);
            return buildResponse("FAQ_ANSWER", "Cần đăng nhập",
                    "Bạn cần đăng nhập để tra cứu đơn hàng.",
                    "ORDER_INQUIRY", "HIGH", true, null, null, List.of(), MAIN_ACTIONS, sessionId);
        }

        Long orderId = extractOrderId(message);
        List<OrderItem> orders = orderId != null
                ? apiToolService.getOrderById(orderId, jwtToken)
                : apiToolService.getMyOrders(jwtToken);
        if (orders == null) {
            logQuery(sessionId, userId, message, "ORDER_INQUIRY", "API", "Loi ket noi", startTime, false);
            return buildResponse("FAQ_ANSWER", "Lỗi tra cứu",
                    "Xin lỗi, không thể tra cứu đơn hàng lúc này. Vui lòng thử lại sau.",
                    "ORDER_INQUIRY", "LOW", true, null, null, List.of(), MAIN_ACTIONS, sessionId);
        }

        if (orders.isEmpty()) {
            logQuery(sessionId, userId, message, "ORDER_INQUIRY", "API", "Chua co don", startTime, false);
            return buildResponse("ORDER_LIST", "Đơn hàng",
                    orderId != null ? "Không tìm thấy đơn hàng phù hợp." : "Bạn hiện chưa có đơn hàng nào.",
                    "ORDER_INQUIRY", "HIGH", true, orders, null, List.of(),
                    List.of(new Action("SEARCH_PRODUCT", "Tìm sản phẩm")), sessionId);
        }

        String summary = buildOrderSummary(orderId, orders);
        logQuery(sessionId, userId, message, "ORDER_INQUIRY", "API", summary, startTime, false);
        return buildResponse("ORDER_LIST", "Đơn hàng của bạn", summary,
                "ORDER_INQUIRY", "HIGH", true, orders, null, List.of(), ORDER_ACTIONS, sessionId);
    }

    private ChatQueryResponse handleProductSearch(
            String message,
            String normalized,
            String understoodQuery,
            Long userId,
            Long sessionId,
            String jwtToken,
            long startTime
    ) {
        String query = understoodQuery == null || understoodQuery.isBlank()
                ? extractProductQuery(normalized, message)
                : understoodQuery;

        // ── Gap 2: Always run both API search and CatalogRag in parallel, merge results ──
        List<ProductItem> apiProducts = apiToolService.searchProducts(query, jwtToken);
        if (apiProducts == null) {
            logQuery(sessionId, userId, message, "PRODUCT_SEARCH", "API", "Loi ket noi", startTime, false);
            return buildResponse("FAQ_ANSWER", "Lỗi tìm kiếm",
                    "Xin lỗi, không thể tìm sản phẩm lúc này. Vui lòng thử lại sau.",
                    "PRODUCT_SEARCH", "LOW", true, null, null, List.of(), MAIN_ACTIONS, sessionId);
        }

        List<CatalogRagService.ProductCandidate> ragCandidates = catalogRagService.retrieveProducts(query, 3);
        List<ProductItem> ragProducts = apiToolService.getProductsByCandidates(ragCandidates, jwtToken);

        // Merge + dedup: API results first, then RAG-only results
        Map<Long, ProductItem> merged = new LinkedHashMap<>();
        for (ProductItem item : apiProducts) {
            if (item.id() != null) {
                merged.putIfAbsent(item.id(), item);
            }
        }
        boolean hasRagContribution = false;
        if (ragProducts != null) {
            for (ProductItem item : ragProducts) {
                if (item.id() != null && merged.putIfAbsent(item.id(), item) == null) {
                    hasRagContribution = true;
                }
            }
        }
        List<ProductItem> products = merged.values().stream().limit(8).toList();

        if (products.isEmpty()) {
            logQuery(sessionId, userId, message, "PRODUCT_SEARCH", "API_RAG", "Khong tim thay", startTime, false);
            return buildResponse("PRODUCT_LIST", "Tìm sản phẩm",
                    "Không tìm thấy sản phẩm phù hợp trong catalog. Bạn có thể thử theo tên thuốc, hoạt chất, nhóm bệnh hoặc danh mục.",
                    "PRODUCT_SEARCH", "MEDIUM", true, null, products, List.of(),
                    List.of(
                            new Action("SEARCH_BY_ACTIVE_INGREDIENT", "Tìm theo hoạt chất"),
                            new Action("SEARCH_BY_DISEASE_GROUP", "Tìm theo nhóm bệnh"),
                            new Action("SEARCH_BY_CATEGORY", "Tìm theo danh mục"),
                            new Action("CHAT_PHARMACIST", "Chat với dược sĩ")
                    ), sessionId);
        }

        // ── Gap 1: RAG Generation — use Gemini to generate natural language answer ──
        String routeUsed = hasRagContribution ? "CATALOG_RAG" : "API";
        String answer = generateProductAnswer(message, query, products);

        // ── Gap 3: Build sources from search routes ──
        List<Source> sources = buildProductSources(apiProducts, hasRagContribution);
        String confidence = hasRagContribution ? "MEDIUM" : "HIGH";

        logQuery(sessionId, userId, message, "PRODUCT_SEARCH", routeUsed, answer, startTime, false);
        return buildResponse("PRODUCT_LIST", "Sản phẩm tìm thấy", answer,
                "PRODUCT_SEARCH", confidence, true, null, products, sources, PRODUCT_ACTIONS, sessionId);
    }

    private ChatQueryResponse handleFaqPolicy(String message, Long userId, Long sessionId, long startTime) {
        List<RagService.ChunkResult> chunks = ragService.retrieve(message, 3);
        double topScore = chunks.isEmpty() ? 0.0 : chunks.get(0).score();

        if (topScore < 0.45) {
            logQuery(sessionId, userId, message, "FAQ_POLICY", "RAG", "Khong du tin cay", startTime, false);
            return buildResponse("OUT_OF_SCOPE", "Không tìm thấy thông tin",
                    "Xin lỗi, tôi chưa tìm thấy thông tin phù hợp trong kho tri thức. Bạn có thể thử hỏi cụ thể hơn.",
                    "FAQ_POLICY", "LOW", true, null, null, List.of(), MAIN_ACTIONS, sessionId);
        }

        List<RagService.ChunkResult> contextChunks = chunks.stream()
                .filter(chunk -> chunk.score() >= 0.35)
                .toList();
        String context = contextChunks.stream()
                .map(RagService.ChunkResult::content)
                .collect(Collectors.joining("\n\n---\n\n"));

        String userPrompt = "CONTEXT:\n" + context + "\n\nCAU HOI: " + message
                + "\n\nHay tra loi dua tren context tren. Neu context khong du, noi ro.";
        String answer = geminiService.generate(SYSTEM_PROMPT, userPrompt);
        if (answer == null || answer.isBlank()) {
            answer = chunks.get(0).content();
        }

        String confidence = topScore >= 0.7 ? "HIGH" : topScore >= 0.5 ? "MEDIUM" : "LOW";
        List<Source> sources = buildSources(contextChunks);

        logQuery(sessionId, userId, message, "FAQ_POLICY", "RAG", answer, startTime, false);
        return buildResponse("FAQ_ANSWER", "Thông tin", answer,
                "FAQ_POLICY", confidence, true, null, null, sources, FAQ_ACTIONS, sessionId);
    }

    private ChatQueryResponse handleFaqFallback(String message, Long userId, Long sessionId, long startTime) {
        List<RagService.ChunkResult> chunks = ragService.retrieve(message, 3);
        double topScore = chunks.isEmpty() ? 0.0 : chunks.get(0).score();

        if (topScore >= 0.45) {
            return handleFaqPolicy(message, userId, sessionId, startTime);
        }

        logQuery(sessionId, userId, message, "OUT_OF_SCOPE", "TEMPLATE", OUT_OF_SCOPE_RESPONSE, startTime, false);
        return buildResponse("OUT_OF_SCOPE", "Ngoài phạm vi", OUT_OF_SCOPE_RESPONSE,
                "OUT_OF_SCOPE", "LOW", true, null, null, List.of(), MAIN_ACTIONS, sessionId);
    }

    private String normalizeQuery(String text) {
        if (text == null) {
            return "";
        }
        String lower = text.toLowerCase().trim();
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('\u0111', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsPhrase(String text, String phrase) {
        String paddedText = " " + text + " ";
        String paddedPhrase = " " + phrase + " ";
        return paddedText.contains(paddedPhrase);
    }

    private Long extractOrderId(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        Matcher matcher = ORDER_ID_PATTERN.matcher(message);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean looksLikeProductQuery(String normalized) {
        if (normalized == null || normalized.isBlank()) {
            return false;
        }
        String[] tokens = normalized.split("\\s+");
        if (tokens.length == 0 || tokens.length > 5) {
            return false;
        }
        if (!PRODUCT_ENTITY_HINT.matcher(normalized).matches()) {
            return false;
        }

        List<String> blockers = List.of(
                "chinh sach", "giao hang", "doi tra", "thanh toan",
                "don hang", "trang thai", "huong dan", "hotline"
        );
        return blockers.stream().noneMatch(blocker -> containsPhrase(normalized, blocker));
    }

    private String extractProductQuery(String normalized, String originalMessage) {
        String query = stripProductQueryPrefixes(normalized);
        if (!query.isBlank() && !query.equals(normalized)) {
            return query;
        }

        if (looksLikeProductQuery(normalized)) {
            return normalized;
        }

        return normalized.isBlank() ? originalMessage : normalized;
    }

    private String stripProductQueryPrefixes(String normalized) {
        if (normalized == null || normalized.isBlank()) {
            return "";
        }

        String result = normalized.trim();
        boolean changed;
        do {
            changed = false;
            for (String prefix : PRODUCT_QUERY_PREFIXES) {
                String normalizedPrefix = normalizeQuery(prefix);
                if (result.startsWith(normalizedPrefix + " ")) {
                    result = result.substring(normalizedPrefix.length()).trim();
                    changed = true;
                    break;
                }
            }
        } while (changed && !result.isBlank());

        return result.isBlank() ? normalized : result;
    }

    private BotSession getOrCreateSession(Long userId) {
        if (userId != null) {
            return sessionRepo.findByUserIdAndStatus(userId, "ACTIVE")
                    .orElseGet(() -> {
                        BotSession session = new BotSession();
                        session.setUserId(userId);
                        session.setSessionToken(UUID.randomUUID().toString());
                        return sessionRepo.save(session);
                    });
        }

        BotSession session = new BotSession();
        session.setSessionToken(UUID.randomUUID().toString());
        return sessionRepo.save(session);
    }

    private void logQuery(Long sessionId, Long userId, String message, String intent,
                          String route, String answer, long startTime, boolean blocked) {
        int elapsed = (int) (System.currentTimeMillis() - startTime);
        BotQueryLog qlog = new BotQueryLog();
        qlog.setSessionId(sessionId);
        qlog.setUserId(userId);
        qlog.setMessage(message);
        qlog.setDetectedIntent(intent);
        qlog.setRouteUsed(route);
        qlog.setResponseText(answer != null && answer.length() > 500 ? answer.substring(0, 500) : answer);
        qlog.setResponseTimeMs(elapsed);
        qlog.setBlockedByGuardrail(blocked);
        queryLogRepo.save(qlog);
    }

    private List<Source> buildSources(List<RagService.ChunkResult> chunks) {
        LinkedHashMap<String, Source> deduped = new LinkedHashMap<>();

        for (RagService.ChunkResult chunk : chunks) {
            if (chunk.sourceTitle() == null || chunk.sourceTitle().isBlank()
                    || chunk.sourceType() == null || chunk.sourceType().isBlank()) {
                continue;
            }
            String key = chunk.sourceType() + "::" + chunk.sourceTitle();
            deduped.putIfAbsent(key, new Source(chunk.sourceTitle(), chunk.sourceType()));
            if (deduped.size() >= 3) {
                break;
            }
        }

        return List.copyOf(deduped.values());
    }

    private ChatQueryResponse buildResponse(
            String type,
            String title,
            String answer,
            String intent,
            String confidenceLevel,
            boolean safe,
            List<OrderItem> orders,
            List<ProductItem> products,
            List<Source> sources,
            List<Action> actions,
            Long sessionId
    ) {
        return new ChatQueryResponse(
                type,
                title,
                answer,
                intent,
                confidenceLevel,
                safe,
                orders == null ? List.of() : orders,
                products == null ? List.of() : products,
                sources == null ? List.of() : sources,
                actions == null ? List.of() : actions,
                sessionId
        );
    }

    private String buildOrderSummary(Long requestedOrderId, List<OrderItem> orders) {
        if (requestedOrderId != null && !orders.isEmpty()) {
            OrderItem order = orders.get(0);
            StringBuilder summary = new StringBuilder("Thông tin đơn hàng #")
                    .append(order.id())
                    .append(": ")
                    .append(order.statusLabel());

            if (order.paymentStatusLabel() != null) {
                summary.append(" - ").append(order.paymentStatusLabel());
            }
            if (order.trackingCode() != null) {
                summary.append(" - Mã vận đơn: ").append(order.trackingCode());
            }
            return summary.toString();
        }

        long shippingCount = orders.stream()
                .filter(order -> "SHIPPING".equals(order.status()))
                .count();
        long pendingCount = orders.stream()
                .filter(order -> "PENDING_APPROVAL".equals(order.status()))
                .count();

        StringBuilder summary = new StringBuilder("Bạn có ")
                .append(orders.size())
                .append(" đơn hàng gần nhất");
        if (shippingCount > 0 || pendingCount > 0) {
            summary.append(" (");
            if (shippingCount > 0) {
                summary.append(shippingCount).append(" đang giao");
            }
            if (shippingCount > 0 && pendingCount > 0) {
                summary.append(", ");
            }
            if (pendingCount > 0) {
                summary.append(pendingCount).append(" chờ xác nhận");
            }
            summary.append(")");
        }
        summary.append(":");
        return summary.toString();
    }

    /**
     * RAG Generation: use Gemini to generate a natural language answer from product context.
     * Falls back to a template answer if Gemini is unavailable.
     */
    private String generateProductAnswer(String originalMessage, String query, List<ProductItem> products) {
        String context = buildProductContext(products);
        String userPrompt = "CONTEXT SAN PHAM DA TIM THAY:\n" + context
                + "\n\nKHACH HANG HOI: " + originalMessage
                + "\n\nHay tra loi ngan gon, than thien, chi gioi thieu rang da tim thay san pham phu hop."
                + " KHONG liet ke tung thuoc, KHONG lap lai gia, hoat chat, mo ta, cach dung hay tac dung phu"
                + " vi giao dien da hien thi chi tiet san pham ben duoi."
                + " TUYET DOI KHONG tu van lieu dung, ke don, hay chon thuoc cho khach."
                + " Nhac khach lien he duoc si neu can tu van su dung.";

        String generated = geminiService.generate(SYSTEM_PROMPT, userPrompt);
        if (generated != null && !generated.isBlank()) {
            return generated;
        }

        // Fallback: template answer when Gemini is unavailable
        return buildProductAnswerFallback(query, products);
    }

    /**
     * Build structured text context from product list for Gemini augmentation.
     */
    private String buildProductContext(List<ProductItem> products) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < products.size(); i++) {
            ProductItem p = products.get(i);
            context.append(i + 1).append(". ").append(p.name());
            if (p.formattedPrice() != null) {
                context.append(" - Gia: ").append(p.formattedPrice());
            }
            if (p.activeIngredient() != null && !p.activeIngredient().isBlank()) {
                context.append(" - Hoat chat: ").append(p.activeIngredient());
            }
            if (p.categoryName() != null && !p.categoryName().isBlank()) {
                context.append(" - Danh muc: ").append(p.categoryName());
            }
            if (p.matchLabel() != null && !p.matchLabel().isBlank()) {
                context.append(" (").append(p.matchLabel()).append(")");
            }
            if (p.description() != null && !p.description().isBlank()) {
                context.append(" - Mo ta: ").append(p.description());
            }
            if (p.usageInstructions() != null && !p.usageInstructions().isBlank()) {
                context.append(" - Cach dung: ").append(p.usageInstructions());
            }
            if (p.sideEffects() != null && !p.sideEffects().isBlank()) {
                context.append(" - Tac dung phu: ").append(p.sideEffects());
            }
            context.append("\n");
        }
        return context.toString();
    }

    private String buildProductAnswerFallback(String query, List<ProductItem> products) {
        StringBuilder answer = new StringBuilder("Tìm thấy ")
                .append(products.size())
                .append(" sản phẩm");
        if (query != null && !query.isBlank()) {
            answer.append(" cho từ khóa '").append(query).append("'");
        }
        answer.append(". Vui lòng xem chi tiết các sản phẩm hiển thị bên dưới.");
        answer.append("\nLưu ý: chatbot chỉ hỗ trợ tìm sản phẩm, không tư vấn liều dùng hay kê đơn. Liên hệ dược sĩ nếu cần tư vấn sử dụng.");
        return answer.toString();
    }

    /**
     * Gap 3: Build source citations for product search results.
     */
    private List<Source> buildProductSources(List<ProductItem> apiProducts, boolean hasRagContribution) {
        List<Source> sources = new java.util.ArrayList<>();
        if (apiProducts != null && !apiProducts.isEmpty()) {
            sources.add(new Source("Catalog Service", "API"));
        }
        if (hasRagContribution) {
            sources.add(new Source("Catalog RAG", "CATALOG_RAG"));
        }
        return List.copyOf(sources);
    }
}
