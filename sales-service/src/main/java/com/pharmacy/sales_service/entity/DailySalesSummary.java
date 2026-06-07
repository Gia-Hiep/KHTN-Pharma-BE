package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_sales_summary")
@Getter
@Setter
public class DailySalesSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    @Column(name = "total_invoices", nullable = false)
    private Integer totalInvoices = 0;

    @Column(name = "paid_invoices", nullable = false)
    private Integer paidInvoices = 0;

    @Column(name = "cancelled_invoices", nullable = false)
    private Integer cancelledInvoices = 0;

    @Column(name = "total_revenue", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_discount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "net_revenue", nullable = false, precision = 18, scale = 2)
    private BigDecimal netRevenue = BigDecimal.ZERO;

    @Column(name = "retail_revenue", precision = 18, scale = 2)
    private BigDecimal retailRevenue = BigDecimal.ZERO;

    @Column(name = "wholesale_revenue", precision = 18, scale = 2)
    private BigDecimal wholesaleRevenue = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
