package com.pharmacy.sales_service.service;

import com.pharmacy.sales_service.dto.DailySalesReport;
import com.pharmacy.sales_service.entity.DailySalesSummary;
import com.pharmacy.sales_service.entity.Invoice;
import com.pharmacy.sales_service.repository.DailySalesSummaryRepo;
import com.pharmacy.sales_service.repository.InvoiceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InvoiceRepo invoiceRepo;
    private final DailySalesSummaryRepo summaryRepo;

    /**
     * Tính báo cáo doanh thu theo ngày (real-time từ invoices)
     */
    @Transactional(readOnly = true)
    public DailySalesReport getDailyReport(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(LocalTime.MAX);

        List<Invoice> invoices = invoiceRepo.findByDateRange(from, to);

        long total = invoices.size();
        long paid = invoices.stream().filter(i -> "PAID".equals(i.getStatus())).count();
        long cancelled = invoices.stream().filter(i -> "CANCELLED".equals(i.getStatus())).count();

        BigDecimal revenue = invoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()))
                .map(Invoice::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = invoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()))
                .map(Invoice::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal net = revenue.subtract(discount);

        return new DailySalesReport(date, total, paid, cancelled, revenue, discount, net);
    }

    /**
     * Lấy báo cáo doanh thu trong khoảng thời gian
     */
    @Transactional(readOnly = true)
    public List<DailySalesReport> getReportRange(LocalDate from, LocalDate to) {
        List<DailySalesReport> reports = new ArrayList<>();
        LocalDate current = from;

        while (!current.isAfter(to)) {
            reports.add(getDailyReport(current));
            current = current.plusDays(1);
        }

        return reports;
    }

    /**
     * Aggregate và lưu báo cáo ngày vào summary table
     * (Nên chạy bằng scheduled job)
     */
    @Transactional
    public DailySalesSummary aggregateAndSave(LocalDate date) {
        DailySalesReport report = getDailyReport(date);

        DailySalesSummary summary = summaryRepo.findByReportDate(date)
                .orElse(new DailySalesSummary());

        summary.setReportDate(date);
        summary.setTotalInvoices(report.totalInvoices().intValue());
        summary.setPaidInvoices(report.paidInvoices().intValue());
        summary.setCancelledInvoices(report.cancelledInvoices().intValue());
        summary.setTotalRevenue(report.totalRevenue());
        summary.setTotalDiscount(report.totalDiscount());
        summary.setNetRevenue(report.netRevenue());

        // Calculate by order type
        LocalDateTime fromDt = date.atStartOfDay();
        LocalDateTime toDt = date.atTime(LocalTime.MAX);
        List<Invoice> invoices = invoiceRepo.findByDateRange(fromDt, toDt);

        BigDecimal retailRevenue = invoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()) && "RETAIL".equals(i.getOrderType()))
                .map(Invoice::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal wholesaleRevenue = invoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()) && "WHOLESALE".equals(i.getOrderType()))
                .map(Invoice::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        summary.setRetailRevenue(retailRevenue);
        summary.setWholesaleRevenue(wholesaleRevenue);

        return summaryRepo.save(summary);
    }

    /**
     * Lấy pre-aggregated summary từ table
     */
    @Transactional(readOnly = true)
    public List<DailySalesSummary> getSavedSummaries(LocalDate from, LocalDate to) {
        return summaryRepo.findByReportDateBetweenOrderByReportDateDesc(from, to);
    }
}
