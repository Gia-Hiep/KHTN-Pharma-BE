package com.pharmacy.inventory_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InboundRequest(
        String refType,      // "PURCHASE"
        String refId,        // purchaseCode hoặc purchaseId dạng string
        Long performedBy,    // userId
        List<Item> items
) {
    public record Item(
            Long medicineId,
            String medicineName,
            String lotNumber,
            LocalDate expiryDate,
            BigDecimal importPrice, // import price per base unit
            int qty
    ) {}
}
