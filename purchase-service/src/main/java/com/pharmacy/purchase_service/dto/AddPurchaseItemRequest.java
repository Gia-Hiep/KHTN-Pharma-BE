package com.pharmacy.purchase_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddPurchaseItemRequest(
        Long medicineId,
        String unitCode,
        String unitLabel,
        Integer conversionFactor,
        String medicineName,
        String lotNumber,
        LocalDate expiryDate,
        BigDecimal importPrice,
        int qty
) {}
