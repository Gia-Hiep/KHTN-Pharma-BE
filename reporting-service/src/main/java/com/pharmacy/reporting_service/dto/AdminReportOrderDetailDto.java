package com.pharmacy.reporting_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminReportOrderDetailDto(
        String source,
        Long id,
        String code,
        LocalDateTime createdAt,
        String customerName,
        String shippingAddress,
        String notes,
        String paymentMethod,
        String paymentStatus,
        String orderStatus,
        String transactionId,
        Long processedBy,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        List<ItemRow> items,
        List<PaymentRow> payments
) {
    public record ItemRow(
            Long medicineId,
            String medicineName,
            Integer qty,
            String unitLabel,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    public record PaymentRow(
            String paymentMethod,
            String status,
            BigDecimal amount,
            String transactionId,
            LocalDateTime paidAt
    ) {}
}
