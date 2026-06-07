package com.pharmacy.reporting_service.service;

import com.pharmacy.reporting_service.client.InventoryClient;
import com.pharmacy.reporting_service.dto.InventoryReportDto;
import com.pharmacy.reporting_service.entity.InventorySnapshot;
import com.pharmacy.reporting_service.repository.InventorySnapshotRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Inventory report — live data từ inventory-service.
 * Không query snapshot DB cho report reads.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryReportService {

    private final InventoryClient inventoryClient;
    private final InventorySnapshotRepo inventorySnapshotRepo; // backward-compat cho upsertSnapshot

    /**
     * Báo cáo tồn kho tổng hợp — gọi inventory-service live
     */
    public InventoryReportDto getInventoryReport(LocalDate date) {
        // Low stock: /inventory/alerts/low-stock
        List<InventoryClient.LowStockDto> lowStockList = inventoryClient.getLowStock();

        // All products: /inventory/summary
        List<InventoryClient.StockSummaryDto> allSummary = inventoryClient.getSummary();

        // Out of stock: availableQty == 0
        List<InventoryClient.StockSummaryDto> outOfStock = allSummary.stream()
                .filter(s -> s.availableQty() <= 0)
                .collect(Collectors.toList());

        int totalProducts = allSummary.size();

        // Build low stock alert rows — use currentQty (actual field in LowStockAlertDto)
        List<InventoryReportDto.StockAlertRow> lowAlerts = lowStockList.stream()
                .map(s -> new InventoryReportDto.StockAlertRow(
                        s.medicineId(),
                        s.medicineName(),
                        (int) s.currentQty(),   // currentQty field
                        "LOW_STOCK"))
                .collect(Collectors.toList());

        // Build out-of-stock alert rows
        List<InventoryReportDto.StockAlertRow> outAlerts = outOfStock.stream()
                .map(s -> new InventoryReportDto.StockAlertRow(
                        s.medicineId(),
                        s.medicineName(),
                        (int) s.availableQty(),
                        "OUT_OF_STOCK"))
                .collect(Collectors.toList());

        return new InventoryReportDto(
                date != null ? date : LocalDate.now(),
                totalProducts,
                lowAlerts.size(),
                outAlerts.size(),
                BigDecimal.ZERO, // stock value requires pricing — not available from inventory-service
                lowAlerts,
                outAlerts
        );
    }

    /**
     * Báo cáo sắp hết hạn — gọi inventory-service live.
     * ExpiryAlertDto: id, medicineId, medicineName, lotNumber, expiryDate, qty
     */
    public List<ExpiryReportItem> getExpiringReport(int days) {
        LocalDate before = LocalDate.now().plusDays(days);
        List<InventoryClient.ExpiryAlertDto> alerts = inventoryClient.getExpiringBefore(before);

        return alerts.stream()
                .filter(a -> a.qty() > 0) // chỉ lô còn hàng
                .map(a -> {
                    int daysRemaining = -1;
                    if (a.expiryDate() != null) {
                        daysRemaining = (int) LocalDate.now().until(a.expiryDate()).getDays();
                    }
                    return new ExpiryReportItem(
                            a.medicineId(),
                            a.medicineName(),
                            a.lotNumber(),
                            a.qty(),
                            a.expiryDate(),
                            daysRemaining
                    );
                })
                .sorted(Comparator.comparingInt(ExpiryReportItem::daysRemaining))
                .collect(Collectors.toList());
    }

    public record ExpiryReportItem(
            Long productId,
            String productName,
            String batchNumber,
            int quantity,
            LocalDate expiryDate,
            int daysRemaining
    ) {}

    /** Backward-compat: upsert snapshot */
    @Transactional
    public InventorySnapshot upsertSnapshot(InventorySnapshot snap) {
        return inventorySnapshotRepo.findBySnapshotDateAndMedicineId(snap.getSnapshotDate(), snap.getMedicineId())
                .map(cur -> {
                    cur.setMedicineName(snap.getMedicineName());
                    cur.setQuantityOnHand(snap.getQuantityOnHand());
                    cur.setQuantitySoldToday(snap.getQuantitySoldToday());
                    cur.setQuantityReceivedToday(snap.getQuantityReceivedToday());
                    cur.setStockValue(snap.getStockValue());
                    cur.setStockStatus(snap.getStockStatus());
                    return inventorySnapshotRepo.save(cur);
                })
                .orElseGet(() -> inventorySnapshotRepo.save(snap));
    }
}
