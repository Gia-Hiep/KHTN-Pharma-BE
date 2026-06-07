package com.pharmacy.reporting_service.service;

import com.pharmacy.reporting_service.client.SalesClient;
import com.pharmacy.reporting_service.dto.SalesReportDto;
import com.pharmacy.reporting_service.entity.SalesSnapshot;
import com.pharmacy.reporting_service.repository.SalesSnapshotRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final SalesClient salesClient;
    private final SalesSnapshotRepo salesSnapshotRepo;

    @Transactional(readOnly = true)
    public SalesReportDto getSalesReport(LocalDate from, LocalDate to, String orderType) {
        LocalDate start = from != null ? from : LocalDate.now();
        LocalDate end = to != null ? to : start;
        String type = normalizeReportOrderType(orderType);

        List<SaleRow> rows = new ArrayList<>();
        salesClient.getInvoices(start, end, null, null).stream()
                .filter(i -> inDateRange(i.createdAt(), start, end))
                .map(this::fromInvoice)
                .forEach(rows::add);

        salesClient.getBuyerOrders().stream()
                .filter(o -> inDateRange(o.createdAt(), start, end))
                .map(this::fromBuyerOrder)
                .forEach(rows::add);

        List<SaleRow> typedRows = rows.stream()
                .filter(r -> "ALL".equals(type) || type.equals(r.orderType()))
                .toList();
        List<SaleRow> paidRows = typedRows.stream()
                .filter(SaleRow::paid)
                .toList();

        BigDecimal totalRevenue = paidRows.stream()
                .map(SaleRow::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = paidRows.stream()
                .map(SaleRow::discount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netRevenue = paidRows.stream()
                .map(SaleRow::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalInvoices = typedRows.size();
        int paidInvoices = paidRows.size();
        int cancelledInvoices = (int) typedRows.stream().filter(SaleRow::cancelled).count();

        double successRate = totalInvoices > 0
                ? Math.round((double) paidInvoices / totalInvoices * 1000.0) / 10.0
                : 0.0;

        long dayCount = Math.max(1, ChronoUnit.DAYS.between(start, end) + 1);
        BigDecimal avgDailyRevenue = netRevenue.divide(BigDecimal.valueOf(dayCount), 2, RoundingMode.HALF_UP);

        Map<LocalDate, List<SaleRow>> byDate = paidRows.stream()
                .collect(Collectors.groupingBy(SaleRow::date));
        List<SalesReportDto.DailyRevenueRow> daily = byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new SalesReportDto.DailyRevenueRow(
                        e.getKey(),
                        sumTotals(e.getValue()),
                        e.getValue().size()))
                .toList();

        List<SalesReportDto.OrderTypeRow> byType = new ArrayList<>();
        if ("ALL".equals(type)) {
            for (String t : List.of("RETAIL", "WHOLESALE", "ONLINE")) {
                List<SaleRow> typeRows = paidRows.stream()
                        .filter(r -> t.equals(r.orderType()))
                        .toList();
                if (!typeRows.isEmpty()) {
                    byType.add(new SalesReportDto.OrderTypeRow(t, sumTotals(typeRows), typeRows.size()));
                }
            }
        }

        return new SalesReportDto(start, end, totalRevenue, totalDiscount, netRevenue,
                totalInvoices, paidInvoices, cancelledInvoices,
                successRate, avgDailyRevenue, daily, byType);
    }

    @Transactional
    public SalesSnapshot upsertSnapshot(SalesSnapshot snap) {
        return salesSnapshotRepo.findByReportDateAndOrderType(snap.getReportDate(), snap.getOrderType())
                .map(cur -> {
                    cur.setTotalInvoices(snap.getTotalInvoices());
                    cur.setPaidInvoices(snap.getPaidInvoices());
                    cur.setCancelledInvoices(snap.getCancelledInvoices());
                    cur.setTotalRevenue(snap.getTotalRevenue());
                    cur.setTotalDiscount(snap.getTotalDiscount());
                    cur.setNetRevenue(snap.getNetRevenue());
                    cur.setTotalItemsSold(snap.getTotalItemsSold());
                    return salesSnapshotRepo.save(cur);
                })
                .orElseGet(() -> salesSnapshotRepo.save(snap));
    }

    private SaleRow fromInvoice(SalesClient.InvoiceDto invoice) {
        BigDecimal discount = money(invoice.discount()).add(money(invoice.couponDiscount()));
        BigDecimal total = money(invoice.total());
        BigDecimal subtotal = invoice.subtotal() != null ? invoice.subtotal() : total.add(discount);
        String orderType = normalizeSourceOrderType(invoice.orderType());
        boolean cancelled = equalsIgnoreCase(invoice.status(), "CANCELLED");
        boolean paid = equalsIgnoreCase(invoice.paymentStatus(), "PAID")
                && (equalsIgnoreCase(invoice.status(), "PAID")
                || equalsIgnoreCase(invoice.status(), "DELIVERED")
                || equalsIgnoreCase(invoice.status(), "COMPLETED"));

        return new SaleRow(invoice.createdAt().toLocalDate(), orderType, subtotal, discount, total, paid, cancelled);
    }

    private SaleRow fromBuyerOrder(SalesClient.BuyerOrderDto order) {
        BigDecimal discount = money(order.discount());
        BigDecimal total = money(order.total());
        BigDecimal subtotal = order.subtotal() != null ? order.subtotal() : total.add(discount);
        boolean cancelled = equalsIgnoreCase(order.paymentStatus(), "REFUNDED")
                || equalsIgnoreCase(order.status(), "REJECTED")
                || equalsIgnoreCase(order.status(), "CANCELLED")
                || equalsIgnoreCase(order.status(), "RETURNED");
        boolean paid = equalsIgnoreCase(order.paymentStatus(), "PAID") && !cancelled;

        return new SaleRow(order.createdAt().toLocalDate(), "ONLINE", subtotal, discount, total, paid, cancelled);
    }

    private BigDecimal sumTotals(List<SaleRow> rows) {
        return rows.stream()
                .map(SaleRow::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean inDateRange(LocalDateTime value, LocalDate from, LocalDate to) {
        if (value == null) {
            return false;
        }
        LocalDate date = value.toLocalDate();
        return !date.isBefore(from) && !date.isAfter(to);
    }

    private String normalizeReportOrderType(String orderType) {
        if (orderType == null || orderType.isBlank() || "ALL".equalsIgnoreCase(orderType)) {
            return "ALL";
        }
        return orderType.toUpperCase();
    }

    private String normalizeSourceOrderType(String orderType) {
        if (orderType == null || orderType.isBlank()) {
            return "RETAIL";
        }
        return orderType.toUpperCase();
    }

    private boolean equalsIgnoreCase(String actual, String expected) {
        return actual != null && actual.equalsIgnoreCase(expected);
    }

    private BigDecimal money(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private record SaleRow(
            LocalDate date,
            String orderType,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal total,
            boolean paid,
            boolean cancelled
    ) {}
}
