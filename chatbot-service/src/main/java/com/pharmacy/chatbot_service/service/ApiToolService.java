package com.pharmacy.chatbot_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.chatbot_service.dto.ChatQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Calls sales-service and catalog-service for real-time lookup.
 * JWT is forwarded when needed for RBAC / owner-check.
 */
@Service
@Slf4j
public class ApiToolService {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private static final Map<String, String> STATUS_MAP = Map.ofEntries(
            Map.entry("PENDING_APPROVAL", "Chờ xác nhận"),
            Map.entry("CONFIRMED", "Đã xác nhận"),
            Map.entry("PICKING", "Đang soạn hàng"),
            Map.entry("PACKING", "Đang đóng gói"),
            Map.entry("SHIPPING", "Đang giao"),
            Map.entry("DELIVERED", "Đã giao"),
            Map.entry("REJECTED", "Đã từ chối"),
            Map.entry("CANCELLED", "Đã hủy"),
            Map.entry("RETURNED", "Đã trả hàng")
    );

    private static final Map<String, String> PAYMENT_STATUS_MAP = Map.of(
            "UNPAID", "Chưa thanh toán",
            "PAID", "Đã thanh toán",
            "REFUNDED", "Đã hoàn tiền"
    );

    private static final List<String> INGREDIENT_HINTS = List.of("hoat chat", "active ingredient");
    private static final List<String> CATEGORY_HINTS = List.of("danh muc", "category", "nhom san pham");
    private static final List<String> DISEASE_HINTS = List.of(
            "nhom benh", "trieu chung", "dau hieu", "benh"
    );
    private static final List<String> SEARCH_PREFIXES = List.of(
            "cho toi xem", "cho toi tim", "tim kiem", "liet ke",
            "co nhung", "san pham cho", "thuoc cho", "nhom benh",
            "trieu chung", "dau hieu", "gia thuoc", "danh muc",
            "san pham", "thuoc", "benh", "tim", "xem", "mua", "gia"
    );

    private final WebClient salesClient;
    private final WebClient catalogClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final NumberFormat currencyFormat;

    public ApiToolService(
            @Value("${sales.service.url}") String salesUrl,
            @Value("${catalog.service.url}") String catalogUrl
    ) {
        this.salesClient = WebClient.builder().baseUrl(salesUrl).build();
        this.catalogClient = WebClient.builder().baseUrl(catalogUrl).build();
        this.currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    }

    public List<ChatQueryResponse.OrderItem> getMyOrders(String jwtToken) {
        try {
            JsonNode orders = readJson(
                    salesClient.get()
                            .uri("/orders")
                            .header(HttpHeaders.AUTHORIZATION, bearer(jwtToken))
            );
            if (orders == null || !orders.isArray() || orders.isEmpty()) {
                return List.of();
            }

            List<ChatQueryResponse.OrderItem> result = new ArrayList<>();
            int count = 0;
            for (JsonNode order : orders) {
                if (count >= 5) {
                    break;
                }
                result.add(mapOrderItem(order));
                count++;
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to get orders: {}", e.getMessage());
            return null;
        }
    }

    public List<ChatQueryResponse.OrderItem> getOrderById(Long orderId, String jwtToken) {
        try {
            JsonNode order = readJson(
                    salesClient.get()
                            .uri("/orders/" + orderId)
                            .header(HttpHeaders.AUTHORIZATION, bearer(jwtToken))
            );
            if (order == null || order.isMissingNode() || order.isNull()) {
                return List.of();
            }
            return List.of(mapOrderItem(order));
        } catch (Exception e) {
            log.error("Failed to get order {}: {}", orderId, e.getMessage());
            return null;
        }
    }

    public List<ChatQueryResponse.ProductItem> searchProducts(String query, String jwtToken) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }

        try {
            List<ChatQueryResponse.ProductItem> smartResults = searchBySmartSearch(normalizedQuery, jwtToken);
            if (!smartResults.isEmpty()) {
                return smartResults.stream()
                        .filter(this::isValidProduct)
                        .limit(8)
                        .toList();
            }

            List<CategoryInfo> categories = fetchCategories(jwtToken);
            Map<Long, String> categoryNames = new LinkedHashMap<>();
            for (CategoryInfo category : categories) {
                if (category.id() != null && category.name() != null && !category.name().isBlank()) {
                    categoryNames.put(category.id(), category.name());
                }
            }

            List<String> candidates = buildSearchCandidates(normalizedQuery);
            JsonNode allMedicines = null;
            List<ChatQueryResponse.ProductItem> results = new ArrayList<>();

            for (String candidate : candidates) {
                SearchIntent intent = detectSearchIntent(candidate, categories);
                switch (intent.mode()) {
                    case "INGREDIENT" -> {
                        if (allMedicines == null) {
                            allMedicines = fetchAllMedicines(jwtToken);
                        }
                        results.addAll(searchByActiveIngredient(intent.term(), allMedicines, categoryNames));
                        results.addAll(searchByName(intent.term(), jwtToken, categoryNames));
                    }
                    case "CATEGORY" -> {
                        if (allMedicines == null) {
                            allMedicines = fetchAllMedicines(jwtToken);
                        }
                        results.addAll(searchByCategory(intent.term(), allMedicines, categories, categoryNames));
                        results.addAll(searchByName(intent.term(), jwtToken, categoryNames));
                    }
                    case "DISEASE_GROUP" -> {
                        results.addAll(searchByDiseaseGroup(intent.term(), jwtToken, categoryNames));
                        results.addAll(searchByName(intent.term(), jwtToken, categoryNames));
                    }
                    default -> {
                        results.addAll(searchByName(intent.term(), jwtToken, categoryNames));
                        if (allMedicines == null) {
                            allMedicines = fetchAllMedicines(jwtToken);
                        }
                        results.addAll(searchByActiveIngredient(intent.term(), allMedicines, categoryNames));
                        results.addAll(searchByDiseaseGroup(intent.term(), jwtToken, categoryNames));
                        results.addAll(searchByCategory(intent.term(), allMedicines, categories, categoryNames));
                    }
                }

                if (results.size() >= 8) {
                    break;
                }
            }

            Map<Long, ChatQueryResponse.ProductItem> deduped = new LinkedHashMap<>();
            for (ChatQueryResponse.ProductItem item : results) {
                if (item.id() != null) {
                    deduped.putIfAbsent(item.id(), item);
                }
            }

            return deduped.values().stream()
                    .filter(this::isValidProduct)
                    .limit(8)
                    .toList();
        } catch (Exception e) {
            log.error("Product search failed: {}", e.getMessage());
            return null;
        }
    }

    public List<ChatQueryResponse.ProductItem> getProductsByCandidates(
            List<CatalogRagService.ProductCandidate> candidates,
            String jwtToken
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        try {
            Map<Long, String> categoryNames = new LinkedHashMap<>();
            for (CategoryInfo category : fetchCategories(jwtToken)) {
                if (category.id() != null && category.name() != null && !category.name().isBlank()) {
                    categoryNames.put(category.id(), category.name());
                }
            }

            Map<Long, ChatQueryResponse.ProductItem> results = new LinkedHashMap<>();
            for (CatalogRagService.ProductCandidate candidate : candidates) {
                if (candidate.productId() == null) {
                    continue;
                }
                JsonNode medicine = readJson(withOptionalAuth(
                        catalogClient.get().uri("/catalog/medicines/" + candidate.productId()),
                        jwtToken
                ));
                ChatQueryResponse.ProductItem item = mapProductItem(
                        medicine,
                        categoryNames,
                        candidate.matchType(),
                        candidate.matchLabel()
                );
                if (item.id() != null && isValidProduct(item)) {
                    results.putIfAbsent(item.id(), item);
                }
            }

            return results.values().stream().limit(8).toList();
        } catch (Exception e) {
            log.warn("Failed to map catalog RAG product candidates: {}", e.getMessage());
            return List.of();
        }
    }

    private ChatQueryResponse.OrderItem mapOrderItem(JsonNode order) {
        Long id = getLongOr(order, "id");
        String rawStatus = getTextOr(order, "status", "UNKNOWN");
        String statusLabel = STATUS_MAP.getOrDefault(rawStatus, rawStatus);
        String total = getTextOr(order, "total", "0");
        String paymentStatus = blankToNull(getTextOr(order, "paymentStatus", null));
        String paymentStatusLabel = paymentStatus == null
                ? null
                : PAYMENT_STATUS_MAP.getOrDefault(paymentStatus, paymentStatus);

        // Parse order line items if present
        List<ChatQueryResponse.OrderLineItem> lineItems = new ArrayList<>();
        JsonNode itemsNode = order.get("items");
        if (itemsNode != null && itemsNode.isArray()) {
            for (JsonNode item : itemsNode) {
                String medicineName = getTextOr(item, "medicineName", null);
                if (medicineName == null || medicineName.isBlank()) {
                    medicineName = getTextOr(item, "productName", "Sản phẩm");
                }
                int qty = item.has("quantity") ? item.get("quantity").asInt(1) : 1;
                String unitLabel = blankToNull(getTextOr(item, "unitLabel", null));
                String linePrice = getTextOr(item, "lineTotal", null);
                if (linePrice == null) {
                    linePrice = getTextOr(item, "subtotal", null);
                }
                lineItems.add(new ChatQueryResponse.OrderLineItem(
                        medicineName,
                        qty,
                        unitLabel,
                        linePrice != null ? formatPrice(linePrice) : null
                ));
            }
        }

        return new ChatQueryResponse.OrderItem(
                id,
                rawStatus,
                statusLabel,
                formatPrice(total),
                paymentStatus,
                paymentStatusLabel,
                formatDateTime(getTextOr(order, "createdAt", null)),
                blankToNull(getTextOr(order, "trackingCode", null)),
                lineItems.isEmpty() ? null : lineItems
        );
    }

    private SearchIntent detectSearchIntent(String normalizedQuery, List<CategoryInfo> categories) {
        if (containsAny(normalizedQuery, INGREDIENT_HINTS)) {
            return new SearchIntent("INGREDIENT", trimIntentPrefix(normalizedQuery, INGREDIENT_HINTS));
        }
        if (containsAny(normalizedQuery, CATEGORY_HINTS)) {
            return new SearchIntent("CATEGORY", trimIntentPrefix(normalizedQuery, CATEGORY_HINTS));
        }
        if (containsAny(normalizedQuery, DISEASE_HINTS)) {
            return new SearchIntent("DISEASE_GROUP", trimIntentPrefix(normalizedQuery, DISEASE_HINTS));
        }

        CategoryInfo directCategory = findMatchingCategory(normalizedQuery, categories);
        if (directCategory != null) {
            return new SearchIntent("CATEGORY", normalize(directCategory.name()));
        }

        return new SearchIntent("GENERAL", normalizedQuery);
    }

    private List<ChatQueryResponse.ProductItem> searchBySmartSearch(String query, String jwtToken) {
        try {
            JsonNode medicines = readJson(
                    withOptionalAuth(
                            catalogClient.get().uri(uriBuilder -> uriBuilder
                                    .path("/catalog/medicines/smart-search")
                                    .queryParam("q", query)
                                    .queryParam("limit", 8)
                                    .build()),
                            jwtToken
                    )
            );
            if (medicines == null || !medicines.isArray()) {
                return List.of();
            }
            return StreamSupport.stream(medicines.spliterator(), false)
                    .map(this::mapSmartProductItem)
                    .toList();
        } catch (Exception e) {
            log.warn("Smart product search unavailable, fallback to legacy search: {}", e.getMessage());
            return List.of();
        }
    }

    private List<String> buildSearchCandidates(String normalizedQuery) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        addSearchCandidate(candidates, normalizedQuery);

        String stripped = stripLeadingSearchPhrases(normalizedQuery);
        addSearchCandidate(candidates, stripped);

        addSearchCandidate(candidates, stripLeadingSearchPhrases(trimIntentPrefix(normalizedQuery, DISEASE_HINTS)));
        addSearchCandidate(candidates, stripLeadingSearchPhrases(trimIntentPrefix(normalizedQuery, INGREDIENT_HINTS)));
        addSearchCandidate(candidates, stripLeadingSearchPhrases(trimIntentPrefix(normalizedQuery, CATEGORY_HINTS)));

        return new ArrayList<>(candidates);
    }

    private void addSearchCandidate(LinkedHashSet<String> candidates, String value) {
        String normalized = normalize(value);
        if (!normalized.isBlank() && normalized.length() >= 2) {
            candidates.add(normalized);
        }
    }

    private String stripLeadingSearchPhrases(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        String result = normalize(query);
        boolean changed;
        do {
            changed = false;
            for (String prefix : SEARCH_PREFIXES) {
                String normalizedPrefix = normalize(prefix);
                if (result.startsWith(normalizedPrefix + " ")) {
                    result = result.substring(normalizedPrefix.length()).trim();
                    changed = true;
                    break;
                }
            }
        } while (changed && !result.isBlank());

        return result.isBlank() ? query : result;
    }

    private List<ChatQueryResponse.ProductItem> searchByName(
            String query,
            String jwtToken,
            Map<Long, String> categoryNames
    ) {
        try {
            JsonNode medicines = readJson(
                    withOptionalAuth(
                            catalogClient.get().uri(uriBuilder -> uriBuilder
                                    .path("/catalog/medicines/search")
                                    .queryParam("q", query)
                                    .build()),
                            jwtToken
                    )
            );
            return parseProductList(medicines, categoryNames, "NAME", "Theo ten thuoc");
        } catch (Exception e) {
            log.error("Product search by name failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ChatQueryResponse.ProductItem> searchByDiseaseGroup(
            String query,
            String jwtToken,
            Map<Long, String> categoryNames
    ) {
        try {
            JsonNode groups = readJson(
                    withOptionalAuth(
                            catalogClient.get().uri(uriBuilder -> uriBuilder
                                    .path("/catalog/disease-groups/search")
                                    .queryParam("q", query)
                                    .build()),
                            jwtToken
                    )
            );
            if (groups == null || !groups.isArray() || groups.isEmpty()) {
                return List.of();
            }

            List<ChatQueryResponse.ProductItem> results = new ArrayList<>();
            int count = 0;
            for (JsonNode group : groups) {
                if (count >= 2) {
                    break;
                }
                Long groupId = getLongOr(group, "id");
                if (groupId == null) {
                    continue;
                }
                String groupName = getTextOr(group, "name", "Nhom benh");
                JsonNode medicines = readJson(
                        withOptionalAuth(
                                catalogClient.get().uri("/catalog/disease-groups/" + groupId + "/medicines"),
                                jwtToken
                        )
                );
                results.addAll(parseProductList(medicines, categoryNames, "DISEASE_GROUP", "Theo nhom benh: " + groupName));
                count++;
            }
            return results;
        } catch (Exception e) {
            log.error("Disease group search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ChatQueryResponse.ProductItem> searchByActiveIngredient(
            String query,
            JsonNode medicines,
            Map<Long, String> categoryNames
    ) {
        if (medicines == null || !medicines.isArray() || query == null || query.isBlank()) {
            return List.of();
        }

        String normalizedTerm = normalize(query);
        return StreamSupport.stream(medicines.spliterator(), false)
                .filter(medicine -> containsNormalized(getTextOr(medicine, "activeIngredient", ""), normalizedTerm))
                .map(medicine -> mapProductItem(medicine, categoryNames, "ACTIVE_INGREDIENT", "Theo hoat chat"))
                .toList();
    }

    private List<ChatQueryResponse.ProductItem> searchByCategory(
            String query,
            JsonNode medicines,
            List<CategoryInfo> categories,
            Map<Long, String> categoryNames
    ) {
        if (medicines == null || !medicines.isArray() || query == null || query.isBlank()) {
            return List.of();
        }

        List<CategoryInfo> matchedCategories = categories.stream()
                .filter(category -> categoryMatches(query, category))
                .toList();
        if (matchedCategories.isEmpty()) {
            return List.of();
        }

        Map<Long, String> matchedNames = new LinkedHashMap<>();
        for (CategoryInfo category : matchedCategories) {
            if (category.id() != null) {
                matchedNames.put(category.id(), category.name());
            }
        }

        return StreamSupport.stream(medicines.spliterator(), false)
                .filter(medicine -> {
                    Long categoryId = getLongOr(medicine, "categoryId");
                    return categoryId != null && matchedNames.containsKey(categoryId);
                })
                .map(medicine -> {
                    Long categoryId = getLongOr(medicine, "categoryId");
                    String categoryName = categoryId != null ? matchedNames.get(categoryId) : null;
                    return mapProductItem(
                            medicine,
                            categoryNames,
                            "CATEGORY",
                            categoryName == null ? "Theo danh muc" : "Theo danh muc: " + categoryName
                    );
                })
                .toList();
    }

    private JsonNode fetchAllMedicines(String jwtToken) {
        return readJson(withOptionalAuth(catalogClient.get().uri("/catalog/medicines"), jwtToken));
    }

    private List<CategoryInfo> fetchCategories(String jwtToken) {
        JsonNode categories = readJson(withOptionalAuth(catalogClient.get().uri("/catalog/categories"), jwtToken));
        if (categories == null || !categories.isArray()) {
            return List.of();
        }

        return StreamSupport.stream(categories.spliterator(), false)
                .map(node -> new CategoryInfo(
                        getLongOr(node, "id"),
                        blankToNull(getTextOr(node, "code", null)),
                        blankToNull(getTextOr(node, "name", null))
                ))
                .filter(category -> category.id() != null && category.name() != null)
                .toList();
    }

    private ChatQueryResponse.ProductItem mapProductItem(
            JsonNode medicine,
            Map<Long, String> categoryNames,
            String matchType,
            String matchLabel
    ) {
        Long id = getLongOr(medicine, "id");
        String name = getTextOr(medicine, "name", "N/A");
        String price = getTextOr(medicine, "salePrice", null);
        Long categoryId = getLongOr(medicine, "categoryId");

        return new ChatQueryResponse.ProductItem(
                id,
                name,
                price != null ? formatPrice(price) : null,
                blankToNull(getTextOr(medicine, "activeIngredient", null)),
                blankToNull(getTextOr(medicine, "description", null)),
                blankToNull(getTextOr(medicine, "usageInstructions", null)),
                blankToNull(getTextOr(medicine, "sideEffects", null)),
                categoryId != null ? categoryNames.get(categoryId) : null,
                matchType,
                matchLabel,
                blankToNull(getTextOr(medicine, "imageUrl", null))
        );
    }

    private ChatQueryResponse.ProductItem mapSmartProductItem(JsonNode medicine) {
        String price = getTextOr(medicine, "salePrice", null);
        return new ChatQueryResponse.ProductItem(
                getLongOr(medicine, "id"),
                getTextOr(medicine, "name", "N/A"),
                price != null ? formatPrice(price) : null,
                blankToNull(getTextOr(medicine, "activeIngredient", null)),
                blankToNull(getTextOr(medicine, "description", null)),
                blankToNull(getTextOr(medicine, "usageInstructions", null)),
                blankToNull(getTextOr(medicine, "sideEffects", null)),
                blankToNull(getTextOr(medicine, "categoryName", null)),
                blankToNull(getTextOr(medicine, "matchType", null)),
                blankToNull(getTextOr(medicine, "matchLabel", null)),
                blankToNull(getTextOr(medicine, "imageUrl", null))
        );
    }

    private List<ChatQueryResponse.ProductItem> parseProductList(
            JsonNode medicines,
            Map<Long, String> categoryNames,
            String matchType,
            String matchLabel
    ) {
        if (medicines == null || !medicines.isArray()) {
            return List.of();
        }

        return StreamSupport.stream(medicines.spliterator(), false)
                .map(medicine -> mapProductItem(medicine, categoryNames, matchType, matchLabel))
                .toList();
    }

    private boolean isValidProduct(ChatQueryResponse.ProductItem item) {
        if (item.name() == null) {
            return false;
        }
        String name = item.name().toLowerCase(Locale.ROOT).trim();
        return name.length() >= 2 && !name.contains("test");
    }

    private boolean categoryMatches(String query, CategoryInfo category) {
        String normalizedQuery = normalize(query);
        String normalizedName = normalize(category.name());
        String normalizedCode = normalize(category.code());
        return containsNormalized(normalizedQuery, normalizedName)
                || containsNormalized(normalizedName, normalizedQuery)
                || (!normalizedCode.isBlank() && containsNormalized(normalizedQuery, normalizedCode));
    }

    private CategoryInfo findMatchingCategory(String query, List<CategoryInfo> categories) {
        return categories.stream()
                .filter(category -> categoryMatches(query, category))
                .findFirst()
                .orElse(null);
    }

    private boolean containsAny(String text, List<String> hints) {
        return hints.stream().anyMatch(hint -> containsPhrase(text, normalize(hint)));
    }

    private boolean containsPhrase(String text, String phrase) {
        if (text == null || text.isBlank() || phrase == null || phrase.isBlank()) {
            return false;
        }
        return (" " + text + " ").contains(" " + phrase + " ");
    }

    private String trimIntentPrefix(String normalizedQuery, List<String> hints) {
        String result = normalizedQuery;
        for (String hint : hints) {
            String normalizedHint = normalize(hint);
            if (containsPhrase(result, normalizedHint)) {
                result = result.replaceFirst("^.*?" + java.util.regex.Pattern.quote(normalizedHint) + "\\s*", "").trim();
                break;
            }
        }
        return result.isBlank() ? normalizedQuery : result;
    }

    private boolean containsNormalized(String source, String target) {
        String normalizedSource = normalize(source);
        String normalizedTarget = normalize(target);
        if (normalizedSource.isBlank() || normalizedTarget.isBlank()) {
            return false;
        }
        return normalizedSource.contains(normalizedTarget);
    }

    private String normalize(String text) {
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

    private String formatDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ORDER_TIME_FORMAT.format(OffsetDateTime.parse(raw));
        } catch (Exception ignored) {
        }
        try {
            return ORDER_TIME_FORMAT.format(LocalDateTime.parse(raw));
        } catch (Exception ignored) {
        }
        return raw;
    }

    private String formatPrice(String rawPrice) {
        try {
            BigDecimal amount = new BigDecimal(rawPrice);
            return currencyFormat.format(amount) + "d";
        } catch (Exception e) {
            return rawPrice + "d";
        }
    }

    private JsonNode readJson(WebClient.RequestHeadersSpec<?> request) {
        String json = request.retrieve()
                .bodyToMono(String.class)
                .timeout(TIMEOUT)
                .block();
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    private WebClient.RequestHeadersSpec<?> withOptionalAuth(WebClient.RequestHeadersSpec<?> request, String jwtToken) {
        if (jwtToken == null || jwtToken.isBlank()) {
            return request;
        }
        return request.header(HttpHeaders.AUTHORIZATION, bearer(jwtToken));
    }

    private String bearer(String jwtToken) {
        return "Bearer " + jwtToken;
    }

    private String getTextOr(JsonNode node, String field, String defaultValue) {
        JsonNode child = node.get(field);
        if (child == null || child.isNull()) {
            return defaultValue;
        }
        return child.asText();
    }

    private Long getLongOr(JsonNode node, String field) {
        JsonNode child = node.get(field);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asLong();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private record SearchIntent(String mode, String term) {}

    private record CategoryInfo(Long id, String code, String name) {}
}
