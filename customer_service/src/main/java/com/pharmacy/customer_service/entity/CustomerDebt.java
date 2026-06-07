package com.pharmacy.customer_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Quản lý công nợ của khách hàng B2B (nhà thuốc)
 */
@Entity
@Table(name = "customer_debts",
        indexes = {
                @Index(name = "idx_debt_customer", columnList = "customer_id"),
                @Index(name = "idx_debt_status", columnList = "status")
        })
@Getter
@Setter
public class CustomerDebt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Invoice ID từ sales-service
     */
    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    /**
     * Mã hóa đơn để display
     */
    @Column(name = "invoice_code", length = 50)
    private String invoiceCode;

    /**
     * Tổng số tiền nợ ban đầu
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Số tiền đã thanh toán
     */
    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * Trạng thái: PENDING, PARTIAL, PAID, OVERDUE
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /**
     * Ngày đến hạn thanh toán
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (paidAmount == null) paidAmount = BigDecimal.ZERO;
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Số tiền còn lại cần thanh toán
     */
    public BigDecimal getRemainingAmount() {
        return amount.subtract(paidAmount);
    }
}
