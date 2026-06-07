package com.pharmacy.reporting_service.service;

import com.pharmacy.reporting_service.client.SalesClient;
import com.pharmacy.reporting_service.dto.AdminReportOrderDetailDto;
import com.pharmacy.reporting_service.dto.AdminReportOrderRowDto;
import com.pharmacy.reporting_service.dto.AdminReportSummaryDto;
import com.pharmacy.reporting_service.dto.AdminReportTopProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final SalesClient salesClient;

    public AdminReportSummaryDto getSummary(LocalDate from, LocalDate to, String source, String paymentStatus) {
        LocalDate start = from != null ? from : LocalDate.now();
        LocalDate end = to != null ? to : start;
        List<OrderBundle> orders = loadOrders(start, end, source, paymentStatus, null, null);

        BigDecimal totalOrderValue = orders.stream()
                .filter(o -> !o.row.cancelledOrRefunded())
                .map(o -> money(o.row.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidAmount = orders.stream()
                .filter(o -> !o.row.cancelledOrRefunded())
                .filter(o -> equalsIgnoreCase(o.row.paymentStatus(), "PAID"))
                .map(o -> money(o.row.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unpaidAmount = orders.stream()
                .filter(o -> !o.row.cancelledOrRefunded())
                .filter(o -> equalsIgnoreCase(o.row.paymentStatus(), "UNPAID"))
                .map(o -> money(o.row.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refundedAmount = orders.stream()
                .filter(o -> equalsIgnoreCase(o.row.paymentStatus(), "REFUNDED"))
                .map(o -> money(o.row.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int paidOrders = (int) orders.stream()
                .filter(o -> !o.row.cancelledOrRefunded())
                .filter(o -> equalsIgnoreCase(o.row.paymentStatus(), "PAID"))
                .count();
        int unpaidOrders = (int) orders.stream()
                .filter(o -> !o.row.cancelledOrRefunded())
                .filter(o -> equalsIgnoreCase(o.row.paymentStatus(), "UNPAID"))
                .count();
        int pendingApproval = (int) orders.stream()
                .filter(o -> !o.row.cancelledOrRefunded())
                .filter(o -> equalsIgnoreCase(o.row.orderStatus(), "PENDING_APPROVAL"))
                .count();
        int cancelledOrRefunded = (int) orders.stream()
                .filter(o -> o.row.cancelledOrRefunded())
                .count();

        List<AdminReportSummaryDto.DailyRow> dailyRows = orders.stream()
                .collect(Collectors.groupingBy(o -> o.row.createdAt().toLocalDate()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> toDailyRow(e.getKey(), e.getValue()))
                .toList();

        return new AdminReportSummaryDto(
                start,
                end,
                totalOrderValue,
                paidAmount,
                unpaidAmount,
                refundedAmount,
                orders.size(),
                paidOrders,
                unpaidOrders,
                pendingApproval,
                cancelledOrRefunded,
                dailyRows
        );
    }

    public List<AdminReportOrderRowDto> getOrders(
            LocalDate from,
            LocalDate to,
            String source,
            String paymentStatus,
            String orderStatus,
            String keyword
    ) {
        LocalDate start = from != null ? from : LocalDate.now();
        LocalDate end = to != null ? to : start;
        return loadOrders(start, end, source, paymentStatus, orderStatus, keyword).stream()
                .map(OrderBundle::row)
                .sorted(Comparator.comparing(AdminReportOrderRowDto::createdAt).reversed())
                .toList();
    }

    public AdminReportOrderDetailDto getOrderDetail(String source, Long id) {
        if (equalsIgnoreCase(source, "ONLINE")) {
            return salesClient.getBuyerOrders().stream()
                    .filter(o -> Objects.equals(o.id(), id))
                    .findFirst()
                    .map(this::toOnlineDetail)
                    .orElse(null);
        }

        SalesClient.InvoiceDto invoice = salesClient.getInvoice(id);
        return invoice != null ? toPosDetail(invoice) : null;
    }

    public List<AdminReportTopProductDto> getTopProducts(LocalDate from, LocalDate to, String source, int limit) {
        LocalDate start = from != null ? from : LocalDate.now();
        LocalDate end = to != null ? to : start;
        int rowLimit = limit > 0 ? limit : 10;
        Map<Long, ProductAccumulator> products = new HashMap<>();

        loadOrders(start, end, source, "PAID", null, null).stream()
                .filter(o -> !o.row.cancelledOrRefunded())
                .forEach(order -> {
                    String orderKey = order.row.source() + ":" + order.row.id();
                    for (AdminReportOrderDetailDto.ItemRow item : order.items()) {
                        if (item.medicineId() == null) continue;
                        ProductAccumulator acc = products.computeIfAbsent(item.medicineId(),
                                ignored -> new ProductAccumulator(item.medicineId(), item.medicineName()));
                        acc.name = firstNonBlank(acc.name, item.medicineName());
                        acc.qty += item.qty() != null ? item.qty() : 0;
                        acc.revenue = acc.revenue.add(money(item.lineTotal()));
                        acc.orderKeys.add(orderKey);
                    }
                });

        final int[] rank = {1};
        return products.values().stream()
                .sorted(Comparator
                        .comparingInt((ProductAccumulator p) -> p.qty).reversed()
                        .thenComparing((ProductAccumulator p) -> p.revenue, Comparator.reverseOrder()))
                .limit(rowLimit)
                .map(p -> new AdminReportTopProductDto(
                        rank[0]++,
                        p.medicineId,
                        p.name != null ? p.name : "ID:" + p.medicineId,
                        p.qty,
                        p.revenue,
                        p.orderKeys.size()))
                .toList();
    }

    private List<OrderBundle> loadOrders(
            LocalDate from,
            LocalDate to,
            String source,
            String paymentStatus,
            String orderStatus,
            String keyword
    ) {
        String sourceFilter = normalize(source);
        String paymentFilter = normalize(paymentStatus);
        String statusFilter = normalize(orderStatus);
        String keywordFilter = keyword != null && !keyword.isBlank()
                ? keyword.trim().toLowerCase(Locale.ROOT)
                : null;

        List<OrderBundle> rows = new ArrayList<>();

        if (!"ONLINE".equals(sourceFilter)) {
            for (SalesClient.InvoiceDto invoice : salesClient.getInvoices(from, to, null, null)) {
                if (!inDateRange(invoice.createdAt(), from, to)) continue;
                OrderBundle bundle = toPosBundle(invoice);
                if (matches(bundle, paymentFilter, statusFilter, keywordFilter)) {
                    rows.add(bundle);
                }
            }
        }

        if (!"POS".equals(sourceFilter)) {
            for (SalesClient.BuyerOrderDto order : salesClient.getBuyerOrders()) {
                if (!inDateRange(order.createdAt(), from, to)) continue;
                OrderBundle bundle = toOnlineBundle(order);
                if (matches(bundle, paymentFilter, statusFilter, keywordFilter)) {
                    rows.add(bundle);
                }
            }
        }

        return rows;
    }

    private OrderBundle toPosBundle(SalesClient.InvoiceDto invoice) {
        List<AdminReportOrderDetailDto.ItemRow> items = salesClient.getInvoiceItems(invoice.id()).stream()
                .map(this::toItemRow)
                .toList();
        List<AdminReportOrderDetailDto.PaymentRow> payments = salesClient.getInvoicePayments(invoice.id()).stream()
                .map(this::toPaymentRow)
                .toList();
        String paymentMethod = payments.stream()
                .filter(p -> equalsIgnoreCase(p.status(), "SUCCESS"))
                .findFirst()
                .map(AdminReportOrderDetailDto.PaymentRow::paymentMethod)
                .orElse(equalsIgnoreCase(invoice.paymentStatus(), "PAID") ? "POS" : "Chua thu");
        boolean cancelledOrRefunded = equalsIgnoreCase(invoice.status(), "CANCELLED")
                || equalsIgnoreCase(invoice.paymentStatus(), "REFUNDED");

        AdminReportOrderRowDto row = new AdminReportOrderRowDto(
                "POS",
                invoice.id(),
                invoice.code(),
                invoice.createdAt(),
                invoice.customerId() != null ? "Khach #" + invoice.customerId() : "Khach le",
                paymentMethod,
                firstNonBlank(invoice.paymentStatus(), "UNPAID"),
                firstNonBlank(invoice.status(), "DRAFT"),
                items.size(),
                money(invoice.total()),
                cancelledOrRefunded
        );
        return new OrderBundle(row, items, payments, money(invoice.subtotal()), money(invoice.discount()), null, null, null);
    }

    private OrderBundle toOnlineBundle(SalesClient.BuyerOrderDto order) {
        List<AdminReportOrderDetailDto.ItemRow> items = order.items() == null
                ? List.of()
                : order.items().stream().map(this::toItemRow).toList();
        String transactionId = firstNonBlank(order.paymentTransactionId(), order.stripePaymentIntentId());
        List<AdminReportOrderDetailDto.PaymentRow> payments = equalsIgnoreCase(order.paymentStatus(), "PAID")
                ? List.of(new AdminReportOrderDetailDto.PaymentRow(
                        order.paymentMethod(),
                        "SUCCESS",
                        money(order.total()),
                        transactionId,
                        order.createdAt()))
                : List.of();
        boolean cancelledOrRefunded = equalsIgnoreCase(order.paymentStatus(), "REFUNDED")
                || equalsIgnoreCase(order.status(), "REJECTED")
                || equalsIgnoreCase(order.status(), "CANCELLED")
                || equalsIgnoreCase(order.status(), "RETURNED");

        AdminReportOrderRowDto row = new AdminReportOrderRowDto(
                "ONLINE",
                order.id(),
                "ORD-" + order.id(),
                order.createdAt(),
                firstNonBlank(order.buyerName(), "Buyer #" + order.buyerId()),
                firstNonBlank(order.paymentMethod(), "COD"),
                firstNonBlank(order.paymentStatus(), "UNPAID"),
                firstNonBlank(order.status(), "PENDING_APPROVAL"),
                items.size(),
                money(order.total()),
                cancelledOrRefunded
        );
        return new OrderBundle(
                row,
                items,
                payments,
                money(order.subtotal()),
                money(order.discount()),
                order.shippingAddress(),
                order.notes(),
                order.processedBy()
        );
    }

    private AdminReportOrderDetailDto toPosDetail(SalesClient.InvoiceDto invoice) {
        OrderBundle bundle = toPosBundle(invoice);
        return new AdminReportOrderDetailDto(
                bundle.row.source(),
                bundle.row.id(),
                bundle.row.code(),
                bundle.row.createdAt(),
                bundle.row.customerName(),
                null,
                null,
                bundle.row.paymentMethod(),
                bundle.row.paymentStatus(),
                bundle.row.orderStatus(),
                bundle.payments.stream()
                        .map(AdminReportOrderDetailDto.PaymentRow::transactionId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null),
                null,
                bundle.subtotal,
                bundle.discount,
                bundle.row.total(),
                bundle.items,
                bundle.payments
        );
    }

    private AdminReportOrderDetailDto toOnlineDetail(SalesClient.BuyerOrderDto order) {
        OrderBundle bundle = toOnlineBundle(order);
        return new AdminReportOrderDetailDto(
                bundle.row.source(),
                bundle.row.id(),
                bundle.row.code(),
                bundle.row.createdAt(),
                bundle.row.customerName(),
                bundle.shippingAddress,
                bundle.notes,
                bundle.row.paymentMethod(),
                bundle.row.paymentStatus(),
                bundle.row.orderStatus(),
                bundle.payments.stream()
                        .map(AdminReportOrderDetailDto.PaymentRow::transactionId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null),
                bundle.processedBy,
                bundle.subtotal,
                bundle.discount,
                bundle.row.total(),
                bundle.items,
                bundle.payments
        );
    }

    private AdminReportSummaryDto.DailyRow toDailyRow(LocalDate date, List<OrderBundle> orders) {
        BigDecimal totalValue = orders.stream()
                .filter(o -> !o.row.cancelledOrRefunded())
                .map(o -> money(o.row.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = orders.stream()
                .filter(o -> !o.row.cancelledOrRefunded())
                .filter(o -> equalsIgnoreCase(o.row.paymentStatus(), "PAID"))
                .map(o -> money(o.row.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unpaid = orders.stream()
                .filter(o -> !o.row.cancelledOrRefunded())
                .filter(o -> equalsIgnoreCase(o.row.paymentStatus(), "UNPAID"))
                .map(o -> money(o.row.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int cancelled = (int) orders.stream().filter(o -> o.row.cancelledOrRefunded()).count();

        return new AdminReportSummaryDto.DailyRow(date, orders.size(), totalValue, paid, unpaid, cancelled);
    }

    private AdminReportOrderDetailDto.ItemRow toItemRow(SalesClient.InvoiceItemDto item) {
        return new AdminReportOrderDetailDto.ItemRow(
                item.medicineId(),
                item.medicineName(),
                item.qty(),
                firstNonBlank(item.unitLabel(), item.unitCode()),
                money(item.unitPrice()),
                money(item.lineTotal())
        );
    }

    private AdminReportOrderDetailDto.ItemRow toItemRow(SalesClient.BuyerOrderItemDto item) {
        return new AdminReportOrderDetailDto.ItemRow(
                item.medicineId(),
                item.medicineName(),
                item.qty(),
                firstNonBlank(item.unitLabel(), item.unitCode()),
                money(item.unitPrice()),
                money(item.lineTotal())
        );
    }

    private AdminReportOrderDetailDto.PaymentRow toPaymentRow(SalesClient.PaymentDto payment) {
        return new AdminReportOrderDetailDto.PaymentRow(
                payment.paymentMethod(),
                payment.status(),
                money(payment.amount()),
                payment.transactionId(),
                payment.paidAt()
        );
    }

    private boolean matches(OrderBundle bundle, String paymentFilter, String statusFilter, String keywordFilter) {
        if (!"ALL".equals(paymentFilter) && !equalsIgnoreCase(bundle.row.paymentStatus(), paymentFilter)) {
            return false;
        }
        if (!"ALL".equals(statusFilter) && !equalsIgnoreCase(bundle.row.orderStatus(), statusFilter)) {
            return false;
        }
        if (keywordFilter == null) {
            return true;
        }
        if (contains(bundle.row.code(), keywordFilter)
                || contains(bundle.row.customerName(), keywordFilter)
                || contains(bundle.row.paymentMethod(), keywordFilter)
                || contains(bundle.row.orderStatus(), keywordFilter)) {
            return true;
        }
        return bundle.items.stream().anyMatch(i -> contains(i.medicineName(), keywordFilter));
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean inDateRange(LocalDateTime value, LocalDate from, LocalDate to) {
        if (value == null) {
            return false;
        }
        LocalDate date = value.toLocalDate();
        return !date.isBefore(from) && !date.isAfter(to);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() || "ALL".equalsIgnoreCase(value)
                ? "ALL"
                : value.toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(String first, String fallback) {
        return first != null && !first.isBlank() ? first : fallback;
    }

    private boolean equalsIgnoreCase(String actual, String expected) {
        return actual != null && actual.equalsIgnoreCase(expected);
    }

    private BigDecimal money(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private record OrderBundle(
            AdminReportOrderRowDto row,
            List<AdminReportOrderDetailDto.ItemRow> items,
            List<AdminReportOrderDetailDto.PaymentRow> payments,
            BigDecimal subtotal,
            BigDecimal discount,
            String shippingAddress,
            String notes,
            Long processedBy
    ) {}

    private static class ProductAccumulator {
        final Long medicineId;
        String name;
        int qty;
        BigDecimal revenue = BigDecimal.ZERO;
        final Set<String> orderKeys = new HashSet<>();

        ProductAccumulator(Long medicineId, String name) {
            this.medicineId = medicineId;
            this.name = name;
        }
    }
}
