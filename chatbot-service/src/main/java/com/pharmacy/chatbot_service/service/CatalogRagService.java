package com.pharmacy.chatbot_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.chatbot_service.entity.BotCatalogChunk;
import com.pharmacy.chatbot_service.repository.BotCatalogChunkRepo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class CatalogRagService {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final double SIMILARITY_THRESHOLD = 0.65;

    private final BotCatalogChunkRepo chunkRepo;
    private final GeminiService geminiService;
    private final WebClient catalogClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Long, double[]> embeddingCache = new ConcurrentHashMap<>();
    private final Map<Long, CatalogChunkMetadata> metadataCache = new ConcurrentHashMap<>();

    public CatalogRagService(
            BotCatalogChunkRepo chunkRepo,
            GeminiService geminiService,
            @Value("${catalog.service.url}") String catalogUrl
    ) {
        this.chunkRepo = chunkRepo;
        this.geminiService = geminiService;
        this.catalogClient = WebClient.builder().baseUrl(catalogUrl).build();
    }

    public record ProductCandidate(Long productId, String matchType, String matchLabel, double score) {}

    private record CatalogChunkMetadata(
            String sourceType,
            Long sourceId,
            String title,
            List<Long> medicineIds
    ) {}

    @PostConstruct
    public void loadEmbeddingsToCache() {
        embeddingCache.clear();
        metadataCache.clear();

        for (BotCatalogChunk chunk : chunkRepo.findByActiveTrueAndEmbeddingIsNotNull()) {
            double[] embedding = geminiService.jsonToEmbedding(chunk.getEmbedding());
            if (embedding == null) {
                continue;
            }
            embeddingCache.put(chunk.getId(), embedding);
            metadataCache.put(chunk.getId(), toMetadata(chunk));
        }

        log.info("Loaded {} catalog RAG embeddings into cache", embeddingCache.size());
    }

    @Transactional
    public int reindexCatalog(String jwtToken) {
        JsonNode medicines = fetchArray("/catalog/medicines", jwtToken);
        JsonNode categories = fetchArray("/catalog/categories", jwtToken);
        JsonNode diseaseGroups = fetchArray("/catalog/disease-groups", jwtToken);
        if (medicines == null || categories == null || diseaseGroups == null) {
            log.warn("Catalog RAG reindex skipped because catalog API returned null");
            return 0;
        }

        Map<Long, JsonNode> categoriesById = indexById(categories);
        Map<Long, JsonNode> activeMedicinesById = indexActiveMedicines(medicines);
        Map<Long, List<Long>> medicineIdsByCategory = groupMedicineIdsByCategory(activeMedicinesById.values());
        Map<Long, List<Long>> medicineIdsByDiseaseGroup = fetchMedicineIdsByDiseaseGroup(diseaseGroups, jwtToken);

        embeddingCache.clear();
        metadataCache.clear();
        chunkRepo.deleteAll();

        int total = 0;
        for (JsonNode medicine : activeMedicinesById.values()) {
            Long id = getLongOr(medicine, "id");
            if (id == null) {
                continue;
            }
            JsonNode category = categoriesById.get(getLongOr(medicine, "categoryId"));
            saveCatalogChunk(
                    "MEDICINE",
                    id,
                    getTextOr(medicine, "name", "San pham"),
                    buildMedicineContent(medicine, category),
                    metadata(Map.of("medicineIds", List.of(id), "medicineId", id))
            );
            total++;
        }

        for (JsonNode category : categories) {
            Long id = getLongOr(category, "id");
            if (id == null) {
                continue;
            }
            List<Long> medicineIds = medicineIdsByCategory.getOrDefault(id, List.of());
            if (medicineIds.isEmpty()) {
                continue;
            }
            saveCatalogChunk(
                    "CATEGORY",
                    id,
                    getTextOr(category, "name", "Danh muc"),
                    buildCategoryContent(category, medicineIds, activeMedicinesById),
                    metadata(Map.of("medicineIds", medicineIds, "categoryId", id))
            );
            total++;
        }

        for (JsonNode group : diseaseGroups) {
            Long id = getLongOr(group, "id");
            if (id == null) {
                continue;
            }
            List<Long> medicineIds = medicineIdsByDiseaseGroup.getOrDefault(id, List.of()).stream()
                    .filter(activeMedicinesById::containsKey)
                    .toList();
            if (medicineIds.isEmpty()) {
                continue;
            }
            saveCatalogChunk(
                    "DISEASE_GROUP",
                    id,
                    getTextOr(group, "name", "Nhom benh"),
                    buildDiseaseGroupContent(group, medicineIds, activeMedicinesById),
                    metadata(Map.of("medicineIds", medicineIds, "diseaseGroupId", id))
            );
            total++;
        }

        log.info("Catalog RAG reindex completed: {} chunks", total);
        return total;
    }

    public List<ProductCandidate> retrieveProducts(String query, int topK) {
        if (query == null || query.isBlank() || topK <= 0) {
            return List.of();
        }
        if (embeddingCache.isEmpty()) {
            loadEmbeddingsToCache();
        }

        double[] queryEmbedding = geminiService.embed(query);
        if (queryEmbedding == null) {
            return List.of();
        }

        Map<Long, ProductCandidate> candidates = new LinkedHashMap<>();
        embeddingCache.entrySet().stream()
                .map(entry -> {
                    CatalogChunkMetadata metadata = metadataCache.get(entry.getKey());
                    double score = cosineSimilarity(queryEmbedding, entry.getValue());
                    if (metadata != null) {
                        log.debug("RAG chunk '{}' score={} (threshold={})", metadata.title(), String.format("%.4f", score), SIMILARITY_THRESHOLD);
                    }
                    return new ChunkCandidate(metadata, score);
                })
                .filter(candidate -> candidate.metadata() != null)
                .filter(candidate -> candidate.score() >= SIMILARITY_THRESHOLD)
                .sorted(Comparator.comparingDouble(ChunkCandidate::score).reversed())
                .limit(topK)
                .forEach(chunk -> {
                    for (Long productId : chunk.metadata().medicineIds()) {
                        ProductCandidate candidate = new ProductCandidate(
                                productId,
                                "CATALOG_RAG",
                                buildMatchLabel(chunk.metadata()),
                                chunk.score()
                        );
                        ProductCandidate existing = candidates.get(productId);
                        if (existing == null || candidate.score() > existing.score()) {
                            candidates.put(productId, candidate);
                        }
                    }
                });

        return candidates.values().stream()
                .sorted(Comparator.comparingDouble(ProductCandidate::score).reversed())
                .limit(topK)
                .toList();
    }

    private record ChunkCandidate(CatalogChunkMetadata metadata, double score) {}

    private BotCatalogChunk saveCatalogChunk(
            String sourceType,
            Long sourceId,
            String title,
            String content,
            String metadataJson
    ) {
        BotCatalogChunk chunk = new BotCatalogChunk();
        chunk.setSourceType(sourceType);
        chunk.setSourceId(sourceId);
        chunk.setTitle(title);
        chunk.setContent(content);
        chunk.setMetadataJson(metadataJson);
        chunk.setActive(true);

        double[] embedding = geminiService.embed(content);
        if (embedding != null) {
            chunk.setEmbedding(geminiService.embeddingToJson(embedding));
        }

        BotCatalogChunk saved = chunkRepo.save(chunk);
        if (embedding != null) {
            embeddingCache.put(saved.getId(), embedding);
            metadataCache.put(saved.getId(), toMetadata(saved));
        }
        return saved;
    }

    private String buildMedicineContent(JsonNode medicine, JsonNode category) {
        return String.join("\n",
                "San pham: " + getTextOr(medicine, "name", ""),
                "Ma: " + getTextOr(medicine, "code", ""),
                "Generic: " + getTextOr(medicine, "genericName", ""),
                "Hoat chat: " + getTextOr(medicine, "activeIngredient", ""),
                "Danh muc: " + getTextOr(category, "name", ""),
                "Dang bao che: " + getTextOr(medicine, "dosageForm", ""),
                "Quy cach: " + getTextOr(medicine, "packageSize", ""),
                "Nha san xuat: " + getTextOr(medicine, "manufacturer", ""),
                "Mo ta: " + getTextOr(medicine, "description", "")
        );
    }

    private String buildCategoryContent(
            JsonNode category,
            List<Long> medicineIds,
            Map<Long, JsonNode> medicinesById
    ) {
        return String.join("\n",
                "Danh muc: " + getTextOr(category, "name", ""),
                "Ma danh muc: " + getTextOr(category, "code", ""),
                "Mo ta: " + getTextOr(category, "description", ""),
                "San pham lien quan: " + productNames(medicineIds, medicinesById)
        );
    }

    private String buildDiseaseGroupContent(
            JsonNode group,
            List<Long> medicineIds,
            Map<Long, JsonNode> medicinesById
    ) {
        return String.join("\n",
                "Nhom benh: " + getTextOr(group, "name", ""),
                "Ma nhom benh: " + getTextOr(group, "code", ""),
                "Mo ta: " + getTextOr(group, "description", ""),
                "Tu khoa: " + getTextOr(group, "keywords", ""),
                "San pham lien quan: " + productNames(medicineIds, medicinesById)
        );
    }

    private String productNames(List<Long> medicineIds, Map<Long, JsonNode> medicinesById) {
        return medicineIds.stream()
                .map(medicinesById::get)
                .filter(node -> node != null && !node.isNull())
                .map(node -> getTextOr(node, "name", ""))
                .filter(name -> !name.isBlank())
                .toList()
                .toString();
    }

    private String buildMatchLabel(CatalogChunkMetadata metadata) {
        return switch (metadata.sourceType()) {
            case "MEDICINE" -> "Theo RAG catalog: " + metadata.title();
            case "CATEGORY" -> "Theo danh muc gan dung: " + metadata.title();
            case "DISEASE_GROUP" -> "Theo nhom benh gan dung: " + metadata.title();
            default -> "Theo RAG catalog";
        };
    }

    private CatalogChunkMetadata toMetadata(BotCatalogChunk chunk) {
        List<Long> medicineIds = new ArrayList<>();
        if ("MEDICINE".equals(chunk.getSourceType())) {
            medicineIds.add(chunk.getSourceId());
        }
        if (chunk.getMetadataJson() != null && !chunk.getMetadataJson().isBlank()) {
            medicineIds.addAll(readMedicineIds(chunk.getMetadataJson()));
        }
        medicineIds = medicineIds.stream().distinct().toList();
        return new CatalogChunkMetadata(chunk.getSourceType(), chunk.getSourceId(), chunk.getTitle(), medicineIds);
    }

    private List<Long> readMedicineIds(String metadataJson) {
        try {
            JsonNode root = objectMapper.readTree(metadataJson);
            JsonNode ids = root.path("medicineIds");
            if (!ids.isArray()) {
                return List.of();
            }
            List<Long> result = new ArrayList<>();
            for (JsonNode id : ids) {
                result.add(id.asLong());
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<Long, JsonNode> indexById(JsonNode array) {
        Map<Long, JsonNode> result = new LinkedHashMap<>();
        if (array == null || !array.isArray()) {
            return result;
        }
        for (JsonNode node : array) {
            Long id = getLongOr(node, "id");
            if (id != null) {
                result.put(id, node);
            }
        }
        return result;
    }

    private Map<Long, JsonNode> indexActiveMedicines(JsonNode medicines) {
        Map<Long, JsonNode> result = new LinkedHashMap<>();
        if (medicines == null || !medicines.isArray()) {
            return result;
        }
        for (JsonNode medicine : medicines) {
            Long id = getLongOr(medicine, "id");
            String status = getTextOr(medicine, "status", "ACTIVE");
            if (id != null && "ACTIVE".equalsIgnoreCase(status)) {
                result.put(id, medicine);
            }
        }
        return result;
    }

    private Map<Long, List<Long>> groupMedicineIdsByCategory(Iterable<JsonNode> medicines) {
        Map<Long, List<Long>> result = new LinkedHashMap<>();
        for (JsonNode medicine : medicines) {
            Long categoryId = getLongOr(medicine, "categoryId");
            Long medicineId = getLongOr(medicine, "id");
            if (categoryId != null && medicineId != null) {
                result.computeIfAbsent(categoryId, ignored -> new ArrayList<>()).add(medicineId);
            }
        }
        return result;
    }

    private Map<Long, List<Long>> fetchMedicineIdsByDiseaseGroup(JsonNode diseaseGroups, String jwtToken) {
        Map<Long, List<Long>> result = new LinkedHashMap<>();
        if (diseaseGroups == null || !diseaseGroups.isArray()) {
            return result;
        }
        for (JsonNode group : diseaseGroups) {
            Long groupId = getLongOr(group, "id");
            if (groupId == null) {
                continue;
            }
            JsonNode medicines = fetchArray("/catalog/disease-groups/" + groupId + "/medicines", jwtToken);
            if (medicines == null || !medicines.isArray()) {
                continue;
            }
            List<Long> ids = StreamSupport.stream(medicines.spliterator(), false)
                    .map(node -> getLongOr(node, "id"))
                    .filter(id -> id != null)
                    .distinct()
                    .toList();
            result.put(groupId, ids);
        }
        return result;
    }

    private String metadata(Map<String, Object> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            return "{}";
        }
    }

    private JsonNode fetchArray(String path, String jwtToken) {
        try {
            WebClient.RequestHeadersSpec<?> request = catalogClient.get().uri(path);
            if (jwtToken != null && !jwtToken.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);
            }
            String json = request.retrieve()
                    .bodyToMono(String.class)
                    .timeout(TIMEOUT)
                    .block();
            JsonNode root = objectMapper.readTree(json);
            return root != null && root.isArray() ? root : null;
        } catch (Exception e) {
            log.warn("Catalog API fetch failed for {}: {}", path, e.getMessage());
            return null;
        }
    }

    private String getTextOr(JsonNode node, String field, String defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        JsonNode child = node.get(field);
        if (child == null || child.isNull()) {
            return defaultValue;
        }
        return child.asText();
    }

    private Long getLongOr(JsonNode node, String field) {
        if (node == null || node.isNull()) {
            return null;
        }
        JsonNode child = node.get(field);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asLong();
    }

    private double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
