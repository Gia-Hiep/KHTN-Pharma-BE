package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name="invoice_items")
@Getter @Setter
public class InvoiceItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name="medicine_id", nullable = false)
    private Long medicineId;
    @Column(name = "medicine_name", length = 255)
    private String medicineName;

    @Column(name = "unit_code", length = 30)
    private String unitCode;

    @Column(name = "unit_label", length = 80)
    private String unitLabel;

    @Column(name = "conversion_factor", nullable = false)
    private Integer conversionFactor = 1;

    @Column(name = "sale_mode", nullable = false, length = 20)
    private String saleMode = "RETAIL";

    @Column(nullable = false)
    private int qty;

    @Column(name="unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name="line_total", nullable = false)
    private BigDecimal lineTotal;
}
