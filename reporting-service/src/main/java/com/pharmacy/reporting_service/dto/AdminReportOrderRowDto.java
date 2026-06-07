package com.pharmacy.reporting_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminReportOrderRowDto(
        String source,
        Long id,
        String code,
        LocalDateTime createdAt,
        String customerName,
        String paymentMethod,
        String paymentStatus,
        String orderStatus,
        Integer itemCount,
        BigDecimal total,
        Boolean cancelledOrRefunded
) {}
