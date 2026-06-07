package com.pharmacy.sales_service.dto;

public record AddItemRequest(
        Long medicineId,
        int qty,
        String unitCode,
        String unitLabel,
        Integer conversionFactor,
        String saleMode
) {}

