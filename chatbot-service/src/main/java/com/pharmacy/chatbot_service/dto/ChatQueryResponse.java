package com.pharmacy.chatbot_service.dto;

import java.util.List;

/**
 * Response chuẩn cho POST /chatbot/query.
 * Frontend render theo "type" field.
 */
public record ChatQueryResponse(
        String type,            // GREETING, ORDER_LIST, PRODUCT_LIST, FAQ_ANSWER, MEDICAL_REFUSAL, OUT_OF_SCOPE
        String title,
        String answer,
        String intent,
        String confidenceLevel, // HIGH, MEDIUM, LOW
        Boolean safe,
        List<OrderItem> orders,
        List<ProductItem> products,
        List<Source> sources,
        List<Action> suggestedActions,
        Long sessionId
) {
    public record OrderItem(
            Long id,
            String status,
            String statusLabel,
            String formattedTotal,
            String paymentStatus,
            String paymentStatusLabel,
            String createdAtLabel,
            String trackingCode,
            List<OrderLineItem> items
    ) {
        public OrderItem(Long id, String status, String statusLabel, String formattedTotal) {
            this(id, status, statusLabel, formattedTotal, null, null, null, null, null);
        }
    }

    public record OrderLineItem(
            String medicineName,
            int quantity,
            String unitLabel,
            String formattedPrice
    ) {}

    public record ProductItem(
            Long id,
            String name,
            String formattedPrice,
            String activeIngredient,
            String description,
            String usageInstructions,
            String sideEffects,
            String categoryName,
            String matchType,
            String matchLabel,
            String imageUrl
    ) {
        public ProductItem(Long id, String name, String formattedPrice) {
            this(id, name, formattedPrice, null, null, null, null, null, null, null, null);
        }

        public ProductItem(
                Long id,
                String name,
                String formattedPrice,
                String activeIngredient,
                String categoryName,
                String matchType,
                String matchLabel,
                String imageUrl
        ) {
            this(id, name, formattedPrice, activeIngredient, null, null, null, categoryName, matchType, matchLabel, imageUrl);
        }
    }

    public record Source(String title, String sourceType) {}
    public record Action(String id, String label) {}
}
