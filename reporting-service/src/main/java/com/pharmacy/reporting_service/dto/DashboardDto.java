package com.pharmacy.reporting_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dashboard tổng quan cho ADMIN/PHARMACIST */
public record DashboardDto(
        LocalDate asOfDate,

        // === DOANH THU ===
        BigDecimal revenueToday,
        BigDecimal revenueThisWeek,
        BigDecimal revenueThisMonth,
        BigDecimal revenueLastMonth,
        /** % thay đổi so với tháng trước */
        Double revenueGrowthRate,

        // === ĐƠN HÀNG ===
        Integer invoicesToday,
        Integer invoicesThisMonth,

        // === TỒN KHO ===
        Integer lowStockAlerts,
        Integer outOfStockAlerts,
        BigDecimal totalStockValue,

        // === MUA HÀNG ===
        BigDecimal purchaseValueThisMonth,
        Integer pendingPurchaseOrders,

        // === TOP THUỐC ===
        String topMedicineThisMonth,
        Integer topMedicineQuantity
) {}
