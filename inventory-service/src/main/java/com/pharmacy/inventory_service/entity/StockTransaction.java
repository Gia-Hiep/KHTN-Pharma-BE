package com.pharmacy.inventory_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
@Getter @Setter
public class StockTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // IN/OUT/ADJUST/EXPIRED/DAMAGED/RESERVE/COMMIT/RELEASE

    @Column(name = "ref_type")
    private String refType; // PURCHASE/INVOICE

    @Column(name = "ref_id")
    private String refId;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(nullable = false)
    private int qty;

    @Column
    private String note;

    @Column(name = "performed_by")
    private Long performedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static StockTransaction of(String type, String refType, String refId, Long medicineId, Long lotId, int qty, Long userId, String note){
        StockTransaction t = new StockTransaction();
        t.type = type;
        t.refType = refType;
        t.refId = refId;
        t.medicineId = medicineId;
        t.lotId = lotId;
        t.qty = qty;
        t.performedBy = userId;
        t.note = note;
        t.createdAt = LocalDateTime.now();
        return t;
    }
}
