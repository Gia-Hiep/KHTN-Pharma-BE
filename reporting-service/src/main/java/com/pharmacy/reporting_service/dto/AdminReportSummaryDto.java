package com.pharmacy.reporting_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AdminReportSummaryDto(
        LocalDate from,
        LocalDate to,
        BigDecimal totalOrderValue,
        BigDecimal paidAmount,
        BigDecimal unpaidAmount,
        BigDecimal refundedAmount,
        Integer totalOrders,
        Integer paidOrders,
        Integer unpaidOrders,
        Integer pendingApprovalOrders,
        Integer cancelledOrRefundedOrders,
        List<DailyRow> dailyBreakdown
) {
    public record DailyRow(
            LocalDate date,
            Integer totalOrders,
            BigDecimal totalOrderValue,
            BigDecimal paidAmount,
            BigDecimal unpaidAmount,
            Integer cancelledOrRefundedOrders
    ) {}
}
