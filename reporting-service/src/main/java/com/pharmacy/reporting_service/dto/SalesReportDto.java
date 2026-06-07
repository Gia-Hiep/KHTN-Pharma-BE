package com.pharmacy.reporting_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Báo cáo doanh thu tổng quan theo khoảng thời gian */
public record SalesReportDto(
        LocalDate from,
        LocalDate to,
        BigDecimal totalRevenue,
        BigDecimal totalDiscount,
        BigDecimal netRevenue,
        Integer totalInvoices,
        Integer paidInvoices,
        Integer cancelledInvoices,
        /** Tỉ lệ thành công */
        Double successRate,
        /** Doanh thu trung bình mỗi ngày */
        BigDecimal avgDailyRevenue,
        /** Chi tiết theo từng ngày */
        List<DailyRevenueRow> dailyBreakdown,
        /** Chi tiết theo order type */
        List<OrderTypeRow> orderTypeBreakdown
) {
    public record DailyRevenueRow(LocalDate date, BigDecimal revenue, Integer invoiceCount) {}
    public record OrderTypeRow(String orderType, BigDecimal revenue, Integer invoiceCount) {}
}
