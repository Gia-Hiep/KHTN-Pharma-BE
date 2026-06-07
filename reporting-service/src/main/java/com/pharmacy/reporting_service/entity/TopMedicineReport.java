package com.pharmacy.reporting_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Báo cáo top thuốc bán chạy theo tháng
 */
@Entity
@Table(name = "top_medicine_reports",
        uniqueConstraints = @UniqueConstraint(columnNames = {"report_month", "medicine_id"}))
@Getter
@Setter
public class TopMedicineReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tháng của báo cáo (ngày đầu tháng) */
    @Column(name = "report_month", nullable = false)
    private LocalDate reportMonth;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(name = "medicine_name", length = 255)
    private String medicineName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "total_quantity_sold")
    private Integer totalQuantitySold = 0;

    @Column(name = "total_revenue", precision = 18, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    /** Xếp hạng trong tháng */
    @Column(name = "rank_in_month")
    private Integer rankInMonth;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
