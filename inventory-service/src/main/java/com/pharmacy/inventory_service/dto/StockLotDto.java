package com.pharmacy.inventory_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StockLotDto(
        Long id,
        Long medicineId,
        String medicineName,
        String lotNumber,
        LocalDate expiryDate,
        BigDecimal importPrice,
        int qtyOnHand,
        int qtyReserved,
        int available,
        LocalDateTime createdAt
) {}
