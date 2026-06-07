package com.pharmacy.reporting_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Báo cáo tồn kho */
public record InventoryReportDto(
        LocalDate reportDate,
        Integer totalProducts,
        Integer lowStockCount,
        Integer outOfStockCount,
        BigDecimal totalStockValue,
        List<StockAlertRow> lowStockAlerts,
        List<StockAlertRow> outOfStockList
) {
    public record StockAlertRow(
            Long medicineId,
            String medicineName,
            Integer quantityOnHand,
            String stockStatus
    ) {}
}
