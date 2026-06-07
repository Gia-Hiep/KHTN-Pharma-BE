package com.pharmacy.chatbot_service.dto;

import java.util.List;

public record ChatResponse(
        String sessionToken,
        String intent,
        String reply,
        Double confidence,
        /**
         * Loại response: TEXT, OPTIONS, MEDICINE_LIST, ESCALATE
         */
        String responseType,
        /**
         * Danh sách options (nếu loại OPTIONS)
         */
        List<String> options,
        /**
         * Các gợi ý quick-reply
         */
        List<String> suggestions
) {}
