package com.pharmacy.chatbot_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    @Value("${gemini.model.generation:gemini-2.5-flash}")
    private String generationModel;

    @Value("${gemini.model.embedding:gemini-embedding-001}")
    private String embeddingModel;

    @Value("${gemini.timeout.seconds:30}")
    private int timeoutSeconds;

    @Bean
    public WebClient geminiWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String getApiKey() { return apiKey; }
    public String getGenerationModel() { return generationModel; }
    public String getEmbeddingModel() { return embeddingModel; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
}
