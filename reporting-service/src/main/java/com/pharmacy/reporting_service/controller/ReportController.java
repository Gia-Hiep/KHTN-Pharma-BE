package com.pharmacy.reporting_service.controller;

import com.pharmacy.reporting_service.client.CatalogClient;
import com.pharmacy.reporting_service.client.SalesClient;
import com.pharmacy.reporting_service.dto.AdminReportOrderDetailDto;
import com.pharmacy.reporting_service.dto.AdminReportOrderRowDto;
import com.pharmacy.reporting_service.dto.AdminReportSummaryDto;
import com.pharmacy.reporting_service.dto.AdminReportTopProductDto;
import com.pharmacy.reporting_service.dto.DashboardDto;
import com.pharmacy.reporting_service.dto.InventoryReportDto;
import com.pharmacy.reporting_service.dto.SalesReportDto;
import com.pharmacy.reporting_service.entity.*;
import com.pharmacy.reporting_service.repository.*;
import com.pharmacy.reporting_service.service.AdminReportService;
import com.pharmacy.reporting_service.service.DashboardService;
import com.pharmacy.reporting_service.service.InventoryReportService;
import com.pharmacy.reporting_service.service.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
public class ReportController {

    private final SalesReportService salesReportService;
    private final AdminReportService adminReportService;
    private final InventoryReportService inventoryReportService;
    private final DashboardService dashboardService;
    private final TopMedicineReportRepo topMedicineReportRepo;
    private final AuditLogRepo auditLogRepo;
    private final CustomerRegionStatsRepo customerRegionStatsRepo;
    private final InventorySnapshotRepo inventorySnapshotRepo;
    private final SalesSnapshotRepo salesSnapshotRepo;
    private final SalesClient salesClient;
    private final CatalogClient catalogClient;

    // ===== DASHBOARD =====

    @GetMapping("/dashboard")
    public DashboardDto getDashboard() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/summary")
    public AdminReportSummaryDto getAdminSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "ALL") String source,
            @RequestParam(defaultValue = "ALL") String paymentStatus
    ) {
        return adminReportService.getSummary(from, to, source, paymentStatus);
    }

    @GetMapping("/orders")
    public List<AdminReportOrderRowDto> getReportOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "ALL") String source,
            @RequestParam(defaultValue = "ALL") String paymentStatus,
            @RequestParam(defaultValue = "ALL") String orderStatus,
            @RequestParam(required = false) String keyword
    ) {
        return adminReportService.getOrders(from, to, source, paymentStatus, orderStatus, keyword);
    }

    @GetMapping("/orders/{source}/{id}")
    public AdminReportOrderDetailDto getReportOrderDetail(
            @PathVariable String source,
            @PathVariable Long id
    ) {
        AdminReportOrderDetailDto detail = adminReportService.getOrderDetail(source, id);
        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay don hang");
        }
        return detail;
    }

    @GetMapping("/top-products")
    public List<AdminReportTopProductDto> getAdminTopProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "ALL") String source,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return adminReportService.getTopProducts(from, to, source, limit);
    }

    // ===== REVENUE REPORTS =====

    /**
     * GET /reports/revenue/daily
     * Params: date (YYYY-MM-DD, default: today), orderType (default: ALL)
     */
    @GetMapping("/revenue/daily")
    public SalesReportDto getDailyRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "ALL") String orderType
    ) {
        LocalDate reportDate = (date != null) ? date : LocalDate.now();
        return salesReportService.getSalesReport(reportDate, reportDate, orderType);
    }

    /**
     * GET /reports/revenue/monthly
     * Params: month (YYYY-MM, e.g. 2026-03), orderType (default: ALL)
     *
     * Chấp nhận cả "2026-03" (YearMonth) và "2026-03-01" (LocalDate đầu tháng).
     */
    @GetMapping("/revenue/monthly")
    public SalesReportDto getMonthlyRevenue(
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "ALL") String orderType
    ) {
        LocalDate start;
        LocalDate end;

        if (month == null || month.isBlank()) {
            YearMonth ym = YearMonth.now();
            start = ym.atDay(1);
            end   = ym.atEndOfMonth();
        } else {
            try {
                // Handle "2026-03" format (FE sends this with <input type="month">)
                if (month.matches("\\d{4}-\\d{2}")) {
                    YearMonth ym = YearMonth.parse(month);
                    start = ym.atDay(1);
                    end   = ym.atEndOfMonth();
                } else {
                    // Handle "2026-03-01" format (first day of month)
                    start = LocalDate.parse(month);
                    end   = YearMonth.from(start).atEndOfMonth();
                }
            } catch (Exception e) {
                // Fallback to current month
                YearMonth ym = YearMonth.now();
                start = ym.atDay(1);
                end   = ym.atEndOfMonth();
            }
        }

        // Không lấy quá ngày hôm nay
        if (end.isAfter(LocalDate.now())) end = LocalDate.now();
        return salesReportService.getSalesReport(start, end, orderType);
    }

    /** Báo cáo doanh thu khoảng thời gian tùy chọn */
    @GetMapping("/sales")
    public SalesReportDto getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "ALL") String orderType
    ) {
        return salesReportService.getSalesReport(from, to, orderType);
    }

    /** Upsert snapshot (backward-compat) */
    @PostMapping("/sales/snapshot")
    @PreAuthorize("hasRole('ADMIN')")
    public SalesSnapshot upsertSalesSnapshot(@RequestBody SalesSnapshot snapshot) {
        return salesReportService.upsertSnapshot(snapshot);
    }

    // ===== TOP PRODUCTS =====

    /**
     * GET /reports/products/top-selling
     * Params: month (YYYY-MM or YYYY-MM-DD), limit (default: 10)
     *
     * Live aggregation: lấy invoices PAID trong tháng, group by medicineId, sum qty.
     */
    @GetMapping("/products/top-selling")
    public List<Map<String, Object>> getTopSellingProducts(
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "10") int limit
    ) {
        LocalDate start;
        LocalDate end;

        if (month == null || month.isBlank()) {
            YearMonth ym = YearMonth.now();
            start = ym.atDay(1);
            end   = ym.atEndOfMonth();
        } else {
            try {
                if (month.matches("\\d{4}-\\d{2}")) {
                    YearMonth ym = YearMonth.parse(month);
                    start = ym.atDay(1);
                    end   = ym.atEndOfMonth();
                } else {
                    start = LocalDate.parse(month);
                    end   = YearMonth.from(start).atEndOfMonth();
                }
            } catch (Exception e) {
                YearMonth ym = YearMonth.now();
                start = ym.atDay(1);
                end   = ym.atEndOfMonth();
            }
        }

        if (end.isAfter(LocalDate.now())) end = LocalDate.now();
        LocalDate reportStart = start;
        LocalDate reportEnd = end;

        List<SalesClient.InvoiceDto> invoices = salesClient.getInvoices(reportStart, reportEnd, null, "PAID");
        List<SalesClient.InvoiceDto> paid = invoices.stream()
                .filter(this::isPaidInvoice)
                .collect(Collectors.toList());
        List<SalesClient.BuyerOrderDto> paidBuyerOrders = salesClient.getBuyerOrders().stream()
                .filter(o -> inDateRange(o.createdAt(), reportStart, reportEnd))
                .filter(this::isPaidBuyerOrder)
                .collect(Collectors.toList());

        if (paid.isEmpty() && paidBuyerOrders.isEmpty()) return Collections.emptyList();

        // Fetch category lookup maps from catalog-service
        Map<Long, Long> medCatMap = catalogClient.getMedicineCategoryMap();
        Map<Long, String> catNameMap = catalogClient.getCategoryNameMap();

        // Accumulate qty + revenue per medicine using invoice items
        Map<Long, long[]> qtyMap = new java.util.HashMap<>();
        Map<Long, java.math.BigDecimal> revMap = new java.util.HashMap<>();
        Map<Long, String> nameMap = new java.util.HashMap<>();

        for (SalesClient.InvoiceDto inv : paid) {
            List<SalesClient.InvoiceItemDto> items = salesClient.getInvoiceItems(inv.id());
            for (SalesClient.InvoiceItemDto item : items) {
                addTopMedicineRow(qtyMap, revMap, nameMap,
                        item.medicineId(), item.medicineName(), item.qty(), item.lineTotal());
            }
        }

        for (SalesClient.BuyerOrderDto order : paidBuyerOrders) {
            if (order.items() == null) continue;
            for (SalesClient.BuyerOrderItemDto item : order.items()) {
                addTopMedicineRow(qtyMap, revMap, nameMap,
                        item.medicineId(), item.medicineName(), item.qty(), item.lineTotal());
            }
        }

        // Resolve category: medicineId → categoryId → category name
        java.util.concurrent.atomic.AtomicInteger rank = new java.util.concurrent.atomic.AtomicInteger(1);
        return qtyMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(limit)
                .map(e -> {
                    Long mid = e.getKey();
                    Long catId = medCatMap.get(mid);
                    String categoryName = (catId != null) ? catNameMap.getOrDefault(catId, "—") : "—";
                    Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("medicineId",        mid);
                    row.put("medicineName",       nameMap.getOrDefault(mid, "—"));
                    row.put("category",           categoryName);
                    row.put("totalQuantitySold",  e.getValue()[0]);
                    row.put("totalRevenue",       revMap.getOrDefault(mid, java.math.BigDecimal.ZERO));
                    row.put("rankInMonth",        rank.getAndIncrement());
                    return row;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /** Nhập top medicine report (backward-compat) */
    @PostMapping("/products/top-selling")
    @PreAuthorize("hasRole('ADMIN')")
    public TopMedicineReport saveTopMedicine(@RequestBody TopMedicineReport report) {
        return topMedicineReportRepo.save(report);
    }

    // ===== CUSTOMERS BY REGION =====

    @GetMapping("/customers/by-region")
    public List<CustomerRegionStats> getCustomersByRegion(
            @RequestParam(required = false) String month
    ) {
        LocalDate reportMonth = parseMonthOrDefault(month);
        return customerRegionStatsRepo.findByReportMonthOrderByTotalRevenueDesc(reportMonth);
    }

    @PostMapping("/customers/by-region")
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerRegionStats saveCustomerRegionStats(@RequestBody CustomerRegionStats stats) {
        return customerRegionStatsRepo.save(stats);
    }

    // ===== INVENTORY =====

    /** Live inventory report from inventory-service */
    @GetMapping("/inventory")
    public InventoryReportDto getInventoryReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return inventoryReportService.getInventoryReport(date);
    }

    /**
     * GET /reports/inventory/expiring — live từ inventory-service
     * Params: days (default: 30)
     */
    @GetMapping("/inventory/expiring")
    public List<InventoryReportService.ExpiryReportItem> getExpiringInventory(
            @RequestParam(defaultValue = "30") int days
    ) {
        return inventoryReportService.getExpiringReport(days);
    }

    /** Upsert inventory snapshot (backward-compat) */
    @PostMapping("/inventory/snapshot")
    @PreAuthorize("hasRole('ADMIN')")
    public InventorySnapshot upsertInventorySnapshot(@RequestBody InventorySnapshot snapshot) {
        return inventoryReportService.upsertSnapshot(snapshot);
    }

    // ===== AUDIT LOGS =====

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditLog> getAuditLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        if (from != null && to != null) {
            return auditLogRepo.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pageable);
        }
        if (serviceName != null && !serviceName.isBlank()) {
            return auditLogRepo.findByServiceNameOrderByCreatedAtDesc(serviceName, pageable);
        }
        if (userId != null) {
            return auditLogRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return auditLogRepo.findAllByOrderByCreatedAtDesc(pageable);
    }

    @PostMapping("/audit-logs")
    public AuditLog createAuditLog(@RequestBody AuditLog auditLog) {
        return auditLogRepo.save(auditLog);
    }

    // ===== Helpers =====

    private LocalDate parseMonthOrDefault(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now().atDay(1);
        }
        try {
            if (month.matches("\\d{4}-\\d{2}")) {
                return YearMonth.parse(month).atDay(1);
            }
            return LocalDate.parse(month).withDayOfMonth(1);
        } catch (Exception e) {
            return YearMonth.now().atDay(1);
        }
    }

    private void addTopMedicineRow(
            Map<Long, long[]> qtyMap,
            Map<Long, java.math.BigDecimal> revMap,
            Map<Long, String> nameMap,
            Long medicineId,
            String medicineName,
            Integer qty,
            java.math.BigDecimal lineTotal
    ) {
        if (medicineId == null || qty == null || qty <= 0) {
            return;
        }
        qtyMap.merge(medicineId, new long[]{qty, 0}, (a, b) -> new long[]{a[0] + b[0], 0});
        revMap.merge(medicineId, lineTotal != null ? lineTotal : java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        if (medicineName != null && !medicineName.isBlank()) {
            nameMap.putIfAbsent(medicineId, medicineName);
        }
    }

    private boolean isPaidInvoice(SalesClient.InvoiceDto invoice) {
        return equalsIgnoreCase(invoice.paymentStatus(), "PAID")
                && (equalsIgnoreCase(invoice.status(), "PAID")
                || equalsIgnoreCase(invoice.status(), "DELIVERED")
                || equalsIgnoreCase(invoice.status(), "COMPLETED"));
    }

    private boolean isPaidBuyerOrder(SalesClient.BuyerOrderDto order) {
        return equalsIgnoreCase(order.paymentStatus(), "PAID")
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
}
