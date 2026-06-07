package com.pharmacy.catalog_service.dto;

import java.math.BigDecimal;

public record UpdateMedicineUnitRequest(
        String unitCode,
        String unitLabel,
        Integer conversionFactor,
        BigDecimal retailPrice,
        BigDecimal wholesalePrice,
        Integer wholesaleMinQty,
        Boolean isBaseUnit,
        Boolean isDefaultSaleUnit,
        Boolean isActive
) {}
