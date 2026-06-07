package com.pharmacy.sales_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Báo cáo doanh thu theo ngày
 */
public record DailySalesReport(
        LocalDate date,
        Long totalInvoices,
        Long paidInvoices,
        Long cancelledInvoices,
        BigDecimal totalRevenue,
        BigDecimal totalDiscount,
        BigDecimal netRevenue
) {}
