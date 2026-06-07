package com.pharmacy.inventory_service.dto;

public record StockSummaryDto(
        Long medicineId,
        String medicineName,
        long totalQty,
        long reservedQty,
        long availableQty
) {}
