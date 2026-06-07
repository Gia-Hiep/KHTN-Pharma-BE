package com.pharmacy.catalog_service.dto;

import java.math.BigDecimal;

public record UpdateMedicineRequest(
        String code,
        String name,
        String genericName,
        String unit,
        Boolean isRx,
        String manufacturer,
        String activeIngredient,
        String dosageForm,
        String packageSize,
        String origin,
        Long categoryId,
        Long defaultSupplierId,
        BigDecimal salePrice,
        String barcode,
        String imageUrl,
        String description,
        String usageInstructions,
        String sideEffects,
        String status
) {}
