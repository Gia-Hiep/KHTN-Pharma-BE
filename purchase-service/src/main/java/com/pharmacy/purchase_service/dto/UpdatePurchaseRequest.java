package com.pharmacy.purchase_service.dto;

public record UpdatePurchaseRequest(
        String code,
        Long supplierId,
        String notes
) {}
