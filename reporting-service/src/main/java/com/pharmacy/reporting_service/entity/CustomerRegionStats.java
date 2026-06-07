package com.pharmacy.reporting_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Thống kê khách hàng theo khu vực (province/city)
 */
@Entity
@Table(name = "customer_region_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"report_month", "region"}))
@Getter
@Setter
public class CustomerRegionStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_month", nullable = false)
    private LocalDate reportMonth;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(name = "customer_count")
    private Integer customerCount = 0;

    @Column(name = "new_customers")
    private Integer newCustomers = 0;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_revenue", precision = 18, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "avg_order_value", precision = 18, scale = 2)
    private BigDecimal avgOrderValue = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
