package com.pharmacy.inventory_service.dto;

public record LotActionRequest(
        Long lotId,
        int qty,
        String reason
) {}
