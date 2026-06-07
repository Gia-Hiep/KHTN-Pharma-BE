package com.pharmacy.purchase_service.dto;

public record CreatePurchaseRequest(
        String code,
        Long supplierId,
        String notes
) {}
