package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="payments")
@Getter @Setter
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="invoice_id", nullable = false)
    private Long invoiceId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name="payment_method", nullable = false)
    private String paymentMethod;

    @Column(name="transaction_id")
    private String transactionId;

    @Column(nullable = false)
    private String status; // PENDING/SUCCESS/FAILED

    @Column(name="paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(columnDefinition = "json")
    private String meta;

    private String notes;
}
