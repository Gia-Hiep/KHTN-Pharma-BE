package com.pharmacy.reporting_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Snapshot doanh thu theo ngày — được aggregate từ sales-service
 */
@Entity
@Table(name = "sales_snapshots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"report_date", "order_type"}))
@Getter
@Setter
public class SalesSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    /** RETAIL, WHOLESALE, ONLINE, ALL */
    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @Column(name = "total_invoices")
    private Integer totalInvoices = 0;

    @Column(name = "paid_invoices")
    private Integer paidInvoices = 0;

    @Column(name = "cancelled_invoices")
    private Integer cancelledInvoices = 0;

    @Column(name = "total_revenue", precision = 18, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_discount", precision = 18, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "net_revenue", precision = 18, scale = 2)
    private BigDecimal netRevenue = BigDecimal.ZERO;

    @Column(name = "total_items_sold")
    private Integer totalItemsSold = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
