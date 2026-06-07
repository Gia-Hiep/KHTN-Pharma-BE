package com.pharmacy.chatbot_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.chatbot_service.config.GeminiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Wrapper cho Gemini API: generation (gemini-2.5-flash) + embedding (gemini-embedding-001).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final WebClient geminiWebClient;
    private final GeminiConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ═══════════════════════════════════════════
    //  GENERATION — gemini-2.5-flash
    // ═══════════════════════════════════════════

    /**
     * Gọi Gemini API để sinh câu trả lời từ prompt.
     * @param systemPrompt system instruction (khóa cứng)
     * @param userPrompt   user message + context
     * @return generated text, hoặc null nếu lỗi
     */
    public String generate(String systemPrompt, String userPrompt) {
        try {
            String url = "/models/" + config.getGenerationModel()
                    + ":generateContent?key=" + config.getApiKey();

            // Build request body theo Gemini API format
            Map<String, Object> body = Map.of(
                    "system_instruction", Map.of(
                            "parts", List.of(Map.of("text", systemPrompt))
                    ),
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", userPrompt)))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.3,
                            "maxOutputTokens", 1024
                    )
            );

            String responseJson = geminiWebClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                    .block();

            return extractText(responseJson);

        } catch (Exception e) {
            log.error("Gemini generation failed: {}", e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════
    //  EMBEDDING — gemini-embedding-001
    // ═══════════════════════════════════════════

    /**
     * Gọi Gemini API để tạo embedding vector cho text.
     * @return double[] embedding (768 dimensions), hoặc null nếu lỗi
     */
    public double[] embed(String text) {
        try {
            String url = "/models/" + config.getEmbeddingModel()
                    + ":embedContent?key=" + config.getApiKey();

            Map<String, Object> body = Map.of(
                    "content", Map.of(
                            "parts", List.of(Map.of("text", text))
                    )
            );

            String responseJson = geminiWebClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                    .block();

            return extractEmbedding(responseJson);

        } catch (Exception e) {
            log.error("Gemini embedding failed: {}", e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════

    /** Extract generated text from Gemini response JSON */
    private String extractText(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            log.error("Failed to parse Gemini generation response: {}", e.getMessage());
            return null;
        }
    }

    /** Extract embedding double[] from Gemini response JSON */
    private double[] extractEmbedding(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode values = root.path("embedding").path("values");
            double[] embedding = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                embedding[i] = values.get(i).asDouble();
            }
            return embedding;
        } catch (Exception e) {
            log.error("Failed to parse Gemini embedding response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert double[] embedding thành JSON string để lưu MySQL.
     */
    public String embeddingToJson(double[] embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse JSON string embedding từ MySQL thành double[].
     */
    public double[] jsonToEmbedding(String json) {
        try {
            return objectMapper.readValue(json, double[].class);
        } catch (Exception e) {
            return null;
        }
    }
}
