package com.pharmacy.chatbot_service.service;

import com.pharmacy.chatbot_service.entity.BotDocument;
import com.pharmacy.chatbot_service.entity.BotDocumentChunk;
import com.pharmacy.chatbot_service.entity.BotFaq;
import com.pharmacy.chatbot_service.repository.BotDocumentChunkRepo;
import com.pharmacy.chatbot_service.repository.BotDocumentRepo;
import com.pharmacy.chatbot_service.repository.BotFaqRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Minimal RAG service:
 * - ingest document/FAQ -> chunk -> embed -> save
 * - cache embeddings in memory
 * - cosine similarity in Java
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final BotDocumentRepo documentRepo;
    private final BotDocumentChunkRepo chunkRepo;
    private final BotFaqRepo faqRepo;
    private final GeminiService geminiService;
    private final GuardrailService guardrailService;

    private final Map<Long, double[]> embeddingCache = new ConcurrentHashMap<>();
    private final Map<Long, String> contentCache = new ConcurrentHashMap<>();
    private final Map<Long, SourceMetadata> sourceCache = new ConcurrentHashMap<>();

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 100;
    private static final double SIMILARITY_THRESHOLD = 0.35;
    private static final Set<String> ALLOWED_SOURCE_TYPES = Set.of("FAQ", "GUIDE", "POLICY");

    public record ChunkResult(
            Long chunkId,
            String content,
            double score,
            String sourceType,
            String sourceTitle
    ) {}

    private record SourceMetadata(String sourceType, String sourceTitle) {}

    @PostConstruct
    public void loadEmbeddingsToCache() {
        embeddingCache.clear();
        contentCache.clear();
        sourceCache.clear();

        Map<Long, BotDocument> documentsById = documentRepo.findByActiveTrue().stream()
                .collect(Collectors.toMap(BotDocument::getId, doc -> doc));
        Map<Long, BotFaq> faqsById = faqRepo.findAllActive().stream()
                .collect(Collectors.toMap(BotFaq::getId, faq -> faq));

        List<BotDocumentChunk> chunks = chunkRepo.findByEmbeddingIsNotNull();
        for (BotDocumentChunk chunk : chunks) {
            if (!isChunkActive(chunk, documentsById.keySet(), faqsById.keySet())) {
                continue;
            }

            double[] embedding = geminiService.jsonToEmbedding(chunk.getEmbedding());
            if (embedding == null) {
                continue;
            }

            cacheChunk(chunk, embedding, resolveMetadata(chunk, documentsById, faqsById));
        }

        log.info("Loaded {} active embeddings into cache", embeddingCache.size());
    }

    @Transactional
    public int importDocument(String title, String sourceType, String content) {
        String normalizedSourceType = normalizeSourceType(sourceType);
        validateDocument(title, normalizedSourceType, content);

        BotDocument doc = new BotDocument();
        doc.setTitle(title.strip());
        doc.setSourceType(normalizedSourceType);
        doc.setContent(content.strip());
        documentRepo.save(doc);

        int count = 0;
        List<String> chunks = chunkText(content);
        for (int i = 0; i < chunks.size(); i++) {
            saveChunkWithEmbedding(doc.getId(), null, normalizedSourceType, doc.getTitle(), i, chunks.get(i));
            count++;
        }

        log.info("Imported document '{}': {} chunks created", title, count);
        return count;
    }

    @Transactional
    public int reindexAll() {
        embeddingCache.clear();
        contentCache.clear();
        sourceCache.clear();
        chunkRepo.deleteAll();

        int totalChunks = 0;

        List<BotDocument> docs = documentRepo.findByActiveTrue();
        for (BotDocument doc : docs) {
            List<String> chunks = chunkText(doc.getContent());
            for (int i = 0; i < chunks.size(); i++) {
                saveChunkWithEmbedding(doc.getId(), null, doc.getSourceType(), doc.getTitle(), i, chunks.get(i));
                totalChunks++;
            }
        }

        List<BotFaq> faqs = faqRepo.findAllActive();
        for (BotFaq faq : faqs) {
            String faqContent = "Cau hoi: " + faq.getQuestion() + "\nTra loi: " + faq.getAnswer();
            saveChunkWithEmbedding(null, faq.getId(), "FAQ", faq.getQuestion(), 0, faqContent);
            totalChunks++;
        }

        log.info("Reindex complete: {} total chunks", totalChunks);
        return totalChunks;
    }

    public List<ChunkResult> retrieve(String query, int topK) {
        if (query == null || query.isBlank() || topK <= 0) {
            return List.of();
        }

        double[] queryEmbedding = geminiService.embed(query);
        if (queryEmbedding == null) {
            log.warn("Failed to embed query, returning empty results");
            return List.of();
        }

        return embeddingCache.entrySet().stream()
                .map(entry -> {
                    SourceMetadata source = sourceCache.get(entry.getKey());
                    return new ChunkResult(
                            entry.getKey(),
                            contentCache.get(entry.getKey()),
                            cosineSimilarity(queryEmbedding, entry.getValue()),
                            source != null ? source.sourceType() : null,
                            source != null ? source.sourceTitle() : null
                    );
                })
                .filter(result -> result.score() > SIMILARITY_THRESHOLD)
                .sorted(Comparator.comparingDouble(ChunkResult::score).reversed())
                .limit(topK)
                .toList();
    }

    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        List<String> segments = Arrays.stream(text.replace("\r", "").split("\n+"))
                .map(String::strip)
                .filter(segment -> !segment.isBlank())
                .toList();

        if (segments.isEmpty()) {
            segments = List.of(text.strip());
        }

        StringBuilder current = new StringBuilder();
        for (String segment : segments) {
            if (segment.length() > CHUNK_SIZE) {
                flushChunk(chunks, current);
                chunks.addAll(splitLongSegment(segment));
                continue;
            }

            if (current.length() == 0) {
                current.append(segment);
                continue;
            }

            if (current.length() + 1 + segment.length() <= CHUNK_SIZE) {
                current.append('\n').append(segment);
                continue;
            }

            String previousChunk = current.toString().strip();
            if (!previousChunk.isBlank()) {
                chunks.add(previousChunk);
            }

            current.setLength(0);
            String overlap = trailingOverlap(previousChunk);
            if (!overlap.isBlank()) {
                current.append(overlap);
                if (current.length() + 1 + segment.length() <= CHUNK_SIZE) {
                    current.append('\n');
                } else {
                    current.setLength(0);
                }
            }
            current.append(segment);
        }

        flushChunk(chunks, current);
        return chunks;
    }

    private BotDocumentChunk saveChunkWithEmbedding(
            Long documentId,
            Long faqId,
            String sourceType,
            String sourceTitle,
            int index,
            String content
    ) {
        BotDocumentChunk chunk = new BotDocumentChunk();
        chunk.setDocumentId(documentId);
        chunk.setFaqId(faqId);
        chunk.setChunkIndex(index);
        chunk.setSourceType(sourceType);
        chunk.setSourceTitle(sourceTitle);
        chunk.setContent(content);

        double[] embedding = geminiService.embed(content);
        if (embedding != null) {
            chunk.setEmbedding(geminiService.embeddingToJson(embedding));
        }
        chunkRepo.save(chunk);

        if (embedding != null) {
            cacheChunk(chunk, embedding, new SourceMetadata(sourceType, sourceTitle));
        }
        return chunk;
    }

    private void cacheChunk(BotDocumentChunk chunk, double[] embedding, SourceMetadata source) {
        embeddingCache.put(chunk.getId(), embedding);
        contentCache.put(chunk.getId(), chunk.getContent());
        if (source != null) {
            sourceCache.put(chunk.getId(), source);
        }
    }

    private SourceMetadata resolveMetadata(
            BotDocumentChunk chunk,
            Map<Long, BotDocument> documentsById,
            Map<Long, BotFaq> faqsById
    ) {
        if (chunk.getSourceType() != null && !chunk.getSourceType().isBlank()
                && chunk.getSourceTitle() != null && !chunk.getSourceTitle().isBlank()) {
            return new SourceMetadata(chunk.getSourceType(), chunk.getSourceTitle());
        }

        if (chunk.getDocumentId() != null) {
            BotDocument doc = documentsById.get(chunk.getDocumentId());
            if (doc != null) {
                return new SourceMetadata(doc.getSourceType(), doc.getTitle());
            }
        }

        if (chunk.getFaqId() != null) {
            BotFaq faq = faqsById.get(chunk.getFaqId());
            if (faq != null) {
                return new SourceMetadata("FAQ", faq.getQuestion());
            }
        }

        return null;
    }

    private boolean isChunkActive(BotDocumentChunk chunk, Set<Long> activeDocumentIds, Set<Long> activeFaqIds) {
        if (chunk.getDocumentId() != null) {
            return activeDocumentIds.contains(chunk.getDocumentId());
        }
        if (chunk.getFaqId() != null) {
            return activeFaqIds.contains(chunk.getFaqId());
        }
        return false;
    }

    private void validateDocument(String title, String sourceType, String content) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Title khong duoc de trong");
        }
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Content khong duoc de trong");
        }
        if (!ALLOWED_SOURCE_TYPES.contains(sourceType)) {
            throw new ResponseStatusException(BAD_REQUEST, "sourceType chi duoc la FAQ, GUIDE hoac POLICY");
        }

        GuardrailService.GuardrailResult safety = guardrailService.checkDocumentContent(sourceType, title, content);
        if (!safety.safe()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Tai lieu co noi dung y te nhay cam va khong duoc dua vao chatbot"
            );
        }
    }

    private String normalizeSourceType(String sourceType) {
        return sourceType == null ? "" : sourceType.trim().toUpperCase();
    }

    private void flushChunk(List<String> chunks, StringBuilder current) {
        String value = current.toString().strip();
        if (!value.isBlank()) {
            chunks.add(value);
        }
        current.setLength(0);
    }

    private List<String> splitLongSegment(String segment) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < segment.length()) {
            int end = Math.min(start + CHUNK_SIZE, segment.length());
            result.add(segment.substring(start, end).strip());
            if (end >= segment.length()) {
                break;
            }
            start = Math.max(end - CHUNK_OVERLAP, start + 1);
        }
        return result;
    }

    private String trailingOverlap(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        int start = Math.max(0, text.length() - CHUNK_OVERLAP);
        return text.substring(start).strip();
    }

    private double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) {
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
        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0 ? 0.0 : dot / denominator;
    }
}
