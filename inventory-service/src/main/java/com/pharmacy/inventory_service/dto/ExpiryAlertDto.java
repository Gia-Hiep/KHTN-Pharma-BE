package com.pharmacy.inventory_service.dto;

import java.time.LocalDate;

public record ExpiryAlertDto(
        Long id,
        Long medicineId,
        String medicineName,
        String lotNumber,
        LocalDate expiryDate,
        int qty
) {}
