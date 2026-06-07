package com.pharmacy.sales_service.controller;

import com.pharmacy.sales_service.dto.DailySalesReport;
import com.pharmacy.sales_service.entity.DailySalesSummary;
import com.pharmacy.sales_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sales/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Báo cáo doanh thu theo ngày (real-time)
     */
    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public DailySalesReport getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return reportService.getDailyReport(date);
    }

    /**
     * Báo cáo doanh thu trong khoảng thời gian
     */
    @GetMapping("/range")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DailySalesReport> getReportRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.getReportRange(from, to);
    }

    /**
     * Aggregate và lưu báo cáo ngày (thường chạy bằng scheduled job)
     */
    @PostMapping("/aggregate")
    @PreAuthorize("hasRole('ADMIN')")
    public DailySalesSummary aggregateDaily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return reportService.aggregateAndSave(date);
    }

    /**
     * Lấy pre-aggregated summaries
     */
    @GetMapping("/summaries")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DailySalesSummary> getSummaries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.getSavedSummaries(from, to);
    }
}
