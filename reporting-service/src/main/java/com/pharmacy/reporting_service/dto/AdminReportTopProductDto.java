package com.pharmacy.reporting_service.dto;

import java.math.BigDecimal;

public record AdminReportTopProductDto(
        Integer rank,
        Long medicineId,
        String medicineName,
        Integer totalQuantitySold,
        BigDecimal totalRevenue,
        Integer orderCount
) {}
