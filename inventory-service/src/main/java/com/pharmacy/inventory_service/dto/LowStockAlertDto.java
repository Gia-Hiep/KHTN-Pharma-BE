package com.pharmacy.inventory_service.dto;

public record LowStockAlertDto(
        Long medicineId,
        String medicineName,
        long currentQty,
        int threshold
) {}
