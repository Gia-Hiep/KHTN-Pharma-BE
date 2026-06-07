package com.pharmacy.reporting_service.service;

import com.pharmacy.reporting_service.client.SalesClient;
import com.pharmacy.reporting_service.dto.DashboardDto;
import com.pharmacy.reporting_service.entity.InventorySnapshot;
import com.pharmacy.reporting_service.entity.PurchaseSnapshot;
import com.pharmacy.reporting_service.repository.InventorySnapshotRepo;
import com.pharmacy.reporting_service.repository.PurchaseSnapshotRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SalesClient salesClient;
    private final InventorySnapshotRepo inventorySnapshotRepo;
    private final PurchaseSnapshotRepo purchaseSnapshotRepo;

    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = monthStart.minusDays(1);

        List<SalesClient.InvoiceDto> invoices = salesClient.getInvoices(lastMonthStart, today, null, null);
        List<SalesClient.BuyerOrderDto> buyerOrders = salesClient.getBuyerOrders().stream()
                .filter(o -> inDateRange(o.createdAt(), lastMonthStart, today))
                .toList();

        BigDecimal revenueToday = getLiveRevenue(today, today, invoices, buyerOrders);
        BigDecimal revenueWeek = getLiveRevenue(weekStart, today, invoices, buyerOrders);
        BigDecimal revenueMonth = getLiveRevenue(monthStart, today, invoices, buyerOrders);
        BigDecimal revenueLastMonth = getLiveRevenue(lastMonthStart, lastMonthEnd, invoices, buyerOrders);

        double growthRate = 0.0;
        if (revenueLastMonth.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = revenueMonth.subtract(revenueLastMonth)
                    .divide(revenueLastMonth, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        int invoicesToday = getLiveOrderCount(today, today, invoices, buyerOrders);
        int invoicesMonth = getLiveOrderCount(monthStart, today, invoices, buyerOrders);

        List<InventorySnapshot> invSnapshots = inventorySnapshotRepo.findBySnapshotDate(today);
        if (invSnapshots.isEmpty()) {
            invSnapshots = inventorySnapshotRepo.findBySnapshotDate(today.minusDays(1));
        }
        int lowStockAlerts = (int) invSnapshots.stream()
                .filter(s -> "LOW_STOCK".equals(s.getStockStatus())).count();
        int outOfStockAlerts = (int) invSnapshots.stream()
                .filter(s -> "OUT_OF_STOCK".equals(s.getStockStatus())).count();
        BigDecimal totalStockValue = invSnapshots.stream()
                .map(InventorySnapshot::getStockValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PurchaseSnapshot> purchaseSnaps = purchaseSnapshotRepo
                .findByReportDateBetweenOrderByReportDateAsc(monthStart, today);
        BigDecimal purchaseValueMonth = purchaseSnaps.stream()
                .map(PurchaseSnapshot::getTotalPurchaseValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int pendingPurchaseOrders = purchaseSnaps.stream()
                .map(PurchaseSnapshot::getPendingOrders)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        TopMedicine topMedicine = getTopMedicine(monthStart, today, invoices, buyerOrders);

        return new DashboardDto(
                today,
                revenueToday, revenueWeek, revenueMonth, revenueLastMonth,
                Math.round(growthRate * 10.0) / 10.0,
                invoicesToday, invoicesMonth,
                lowStockAlerts, outOfStockAlerts, totalStockValue,
                purchaseValueMonth, pendingPurchaseOrders,
                topMedicine.name(), topMedicine.qty()
        );
    }

    private BigDecimal getLiveRevenue(
            LocalDate from,
            LocalDate to,
            List<SalesClient.InvoiceDto> invoices,
            List<SalesClient.BuyerOrderDto> buyerOrders
    ) {
        BigDecimal invoiceRevenue = invoices.stream()
                .filter(i -> inDateRange(i.createdAt(), from, to))
                .filter(this::isPaidInvoice)
                .map(i -> money(i.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal onlineRevenue = buyerOrders.stream()
                .filter(o -> inDateRange(o.createdAt(), from, to))
                .filter(this::isPaidBuyerOrder)
                .map(o -> money(o.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return invoiceRevenue.add(onlineRevenue);
    }

    private int getLiveOrderCount(
            LocalDate from,
            LocalDate to,
            List<SalesClient.InvoiceDto> invoices,
            List<SalesClient.BuyerOrderDto> buyerOrders
    ) {
        long posOrders = invoices.stream()
                .filter(i -> inDateRange(i.createdAt(), from, to))
                .filter(this::isPaidInvoice)
                .count();

        long onlineOrders = buyerOrders.stream()
                .filter(o -> inDateRange(o.createdAt(), from, to))
                .filter(this::isCountableBuyerOrder)
                .count();

        return Math.toIntExact(posOrders + onlineOrders);
    }

    private TopMedicine getTopMedicine(
            LocalDate from,
            LocalDate to,
            List<SalesClient.InvoiceDto> invoices,
            List<SalesClient.BuyerOrderDto> buyerOrders
    ) {
        Map<Long, Integer> qtyByMedicine = new HashMap<>();
        Map<Long, String> nameByMedicine = new HashMap<>();

        invoices.stream()
                .filter(i -> inDateRange(i.createdAt(), from, to))
                .filter(this::isPaidInvoice)
                .filter(i -> i.id() != null)
                .forEach(invoice -> salesClient.getInvoiceItems(invoice.id()).forEach(item ->
                        addMedicineQty(qtyByMedicine, nameByMedicine,
                                item.medicineId(), item.medicineName(), item.qty())));

        buyerOrders.stream()
                .filter(o -> inDateRange(o.createdAt(), from, to))
                .filter(this::isPaidBuyerOrder)
                .map(SalesClient.BuyerOrderDto::items)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .forEach(item -> addMedicineQty(qtyByMedicine, nameByMedicine,
                        item.medicineId(), item.medicineName(), item.qty()));

        return qtyByMedicine.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> new TopMedicine(
                        nameByMedicine.getOrDefault(e.getKey(), "ID:" + e.getKey()),
                        e.getValue()))
                .orElse(new TopMedicine("N/A", 0));
    }

    private void addMedicineQty(
            Map<Long, Integer> qtyByMedicine,
            Map<Long, String> nameByMedicine,
            Long medicineId,
            String medicineName,
            Integer qty
    ) {
        if (medicineId == null || qty == null || qty <= 0) {
            return;
        }
        qtyByMedicine.merge(medicineId, qty, Integer::sum);
        if (medicineName != null && !medicineName.isBlank()) {
            nameByMedicine.putIfAbsent(medicineId, medicineName);
        }
    }

    private boolean isPaidInvoice(SalesClient.InvoiceDto invoice) {
        return equalsIgnoreCase(invoice.paymentStatus(), "PAID")
                && (equalsIgnoreCase(invoice.status(), "PAID")
                || equalsIgnoreCase(invoice.status(), "DELIVERED")
                || equalsIgnoreCase(invoice.status(), "COMPLETED"));
    }

    private boolean isPaidBuyerOrder(SalesClient.BuyerOrderDto order) {
        return equalsIgnoreCase(order.paymentStatus(), "PAID") && isCountableBuyerOrder(order);
    }

    private boolean isCountableBuyerOrder(SalesClient.BuyerOrderDto order) {
        return !equalsIgnoreCase(order.paymentStatus(), "REFUNDED")
                && !equalsIgnoreCase(order.status(), "REJECTED")
                && !equalsIgnoreCase(order.status(), "CANCELLED")
                && !equalsIgnoreCase(order.status(), "RETURNED");
    }

    private boolean inDateRange(LocalDateTime value, LocalDate from, LocalDate to) {
        if (value == null) {
            return false;
        }
        LocalDate date = value.toLocalDate();
        return !date.isBefore(from) && !date.isAfter(to);
    }

    private boolean equalsIgnoreCase(String actual, String expected) {
        return actual != null && actual.equalsIgnoreCase(expected);
    }

    private BigDecimal money(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private record TopMedicine(String name, int qty) {}
}
