package com.pharmacy.catalog_service.dto;

import java.math.BigDecimal;

public record SmartMedicineSearchResponse(
        Long id,
        String code,
        String name,
        String genericName,
        BigDecimal salePrice,
        String activeIngredient,
        String description,
        String usageInstructions,
        String sideEffects,
        Long categoryId,
        String categoryName,
        String imageUrl,
        String status,
        String matchType,
        String matchLabel,
        String matchedKeyword,
        int score
) {}
