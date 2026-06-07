package com.pharmacy.sales_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="invoice_lot_allocations")
@Getter @Setter
public class InvoiceLotAllocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="invoice_item_id", nullable = false)
    private Long invoiceItemId;

    @Column(name="lot_id", nullable = false)
    private Long lotId; // inventory_db.stock_lots.id

    @Column(nullable = false)
    private int qty;
}
