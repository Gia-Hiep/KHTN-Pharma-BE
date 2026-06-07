package com.pharmacy.inventory_service.dto;

public record AdjustRequest(
        Long lotId,
        int qtyChange,
        String reason
) {}
