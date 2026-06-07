package com.pharmacy.purchase_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePurchaseItemRequest(
        Long medicineId,
        String unitCode,
        String unitLabel,
        Integer conversionFactor,
        String lotNumber,
        LocalDate expiryDate,
        BigDecimal importPrice,
        Integer qty
) {}
