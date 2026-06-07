package com.pharmacy.purchase_service.service;

import com.pharmacy.purchase_service.client.InventoryClient;
import com.pharmacy.purchase_service.dto.*;
import com.pharmacy.purchase_service.entity.Purchase;
import com.pharmacy.purchase_service.entity.PurchaseItem;
import com.pharmacy.purchase_service.repository.PurchaseItemRepo;
import com.pharmacy.purchase_service.repository.PurchaseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepo purchaseRepo;
    private final PurchaseItemRepo itemRepo;
    private final InventoryClient inventoryClient;

    /* =======================
       CREATE / READ
       ======================= */

    @Transactional
    public Purchase create(CreatePurchaseRequest req, Long userId){
        Purchase p = new Purchase();
        // Auto-generate code if not provided
        String code = req.code();
        if (code == null || code.isBlank()) {
            code = "PO-" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .format(LocalDateTime.now())
                    + "-" + String.format("%04d", new java.util.Random().nextInt(10000));
        }
        p.setCode(code);
        p.setSupplierId(req.supplierId());
        p.setCreatedBy(userId);
        p.setStatus("DRAFT");
        p.setTotal(BigDecimal.ZERO);
        p.setCreatedAt(LocalDateTime.now());
        p.setNotes(req.notes());
        return purchaseRepo.save(p);
    }

    @Transactional(readOnly = true)
    public Purchase get(Long id){
        return purchaseRepo.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<PurchaseItem> items(Long purchaseId){
        return itemRepo.findByPurchaseId(purchaseId);
    }

    /* =======================
       LIST + FILTER
       ======================= */

    @Transactional(readOnly = true)
    public List<Purchase> list(String status, Long supplierId, LocalDateTime from, LocalDateTime to){
        return purchaseRepo.search(status, supplierId, from, to);
    }

    /* =======================
       UPDATE / CANCEL (DRAFT only)
       ======================= */

    @Transactional
    public Purchase update(Long id, UpdatePurchaseRequest req, Long userId){
        Purchase p = purchaseRepo.findById(id).orElseThrow();
        if (!"DRAFT".equals(p.getStatus())) throw new RuntimeException("Purchase not DRAFT");

        if (req.code() != null && !req.code().isBlank()) p.setCode(req.code());
        if (req.supplierId() != null) p.setSupplierId(req.supplierId());
        if (req.notes() != null) p.setNotes(req.notes());

        return purchaseRepo.save(p);
    }

    @Transactional
    public Purchase cancel(Long id, String reason, Long userId){
        Purchase p = purchaseRepo.findById(id).orElseThrow();
        if (!"DRAFT".equals(p.getStatus())) throw new RuntimeException("Purchase not DRAFT");

        p.setStatus("CANCELLED");
        if (reason != null && !reason.isBlank()){
            p.setNotes(reason);
        }
        return purchaseRepo.save(p);
    }

    /* =======================
       ITEMS CRUD (DRAFT only)
       ======================= */

    @Transactional
    public PurchaseItem addItem(Long purchaseId, AddPurchaseItemRequest req, Long userId){
        Purchase p = purchaseRepo.findById(purchaseId).orElseThrow();
        if (!"DRAFT".equals(p.getStatus())) throw new RuntimeException("Purchase not DRAFT");
        if (req.qty() <= 0) throw new RuntimeException("qty must be > 0");
        if (req.importPrice() == null) throw new RuntimeException("importPrice required");
        if (req.expiryDate() == null) throw new RuntimeException("expiryDate required");
        if (req.lotNumber() == null || req.lotNumber().isBlank()) throw new RuntimeException("lotNumber required");
        if (req.unitCode() == null || req.unitCode().isBlank()) throw new RuntimeException("unitCode required");
        if (req.unitLabel() == null || req.unitLabel().isBlank()) throw new RuntimeException("unitLabel required");

        PurchaseItem it = new PurchaseItem();
        it.setPurchaseId(purchaseId);
        it.setMedicineId(req.medicineId());
        it.setUnitCode(req.unitCode());
        it.setUnitLabel(req.unitLabel());
        it.setConversionFactor(req.conversionFactor() != null && req.conversionFactor() > 0 ? req.conversionFactor() : 1);
        it.setMedicineName(req.medicineName());
        it.setLotNumber(req.lotNumber());
        it.setExpiryDate(req.expiryDate());
        it.setImportPrice(req.importPrice());
        it.setQty(req.qty());
        it.setLineTotal(req.importPrice().multiply(BigDecimal.valueOf(req.qty())));

        PurchaseItem saved = itemRepo.save(it);
        recalcTotal(purchaseId);
        return saved;
    }

    @Transactional
    public PurchaseItem updateItem(Long purchaseId, Long itemId, UpdatePurchaseItemRequest req, Long userId){
        Purchase p = purchaseRepo.findById(purchaseId).orElseThrow();
        if (!"DRAFT".equals(p.getStatus())) throw new RuntimeException("Purchase not DRAFT");

        PurchaseItem it = itemRepo.findById(itemId).orElseThrow();
        if (!it.getPurchaseId().equals(purchaseId)) throw new RuntimeException("Item not belong to purchase");

        if (req.medicineId() != null) it.setMedicineId(req.medicineId());
        if (req.unitCode() != null && !req.unitCode().isBlank()) it.setUnitCode(req.unitCode());
        if (req.unitLabel() != null && !req.unitLabel().isBlank()) it.setUnitLabel(req.unitLabel());
        if (req.conversionFactor() != null && req.conversionFactor() > 0) it.setConversionFactor(req.conversionFactor());
        if (req.lotNumber() != null && !req.lotNumber().isBlank()) it.setLotNumber(req.lotNumber());
        if (req.expiryDate() != null) it.setExpiryDate(req.expiryDate());
        if (req.importPrice() != null) it.setImportPrice(req.importPrice());
        if (req.qty() != null) {
            if (req.qty() <= 0) throw new RuntimeException("qty must be > 0");
            it.setQty(req.qty());
        }

        // recompute line_total
        BigDecimal price = it.getImportPrice() == null ? BigDecimal.ZERO : it.getImportPrice();
        int qty = it.getQty();
        it.setLineTotal(price.multiply(BigDecimal.valueOf(qty)));

        PurchaseItem saved = itemRepo.save(it);
        recalcTotal(purchaseId);
        return saved;
    }

    @Transactional
    public void deleteItem(Long purchaseId, Long itemId, Long userId){
        Purchase p = purchaseRepo.findById(purchaseId).orElseThrow();
        if (!"DRAFT".equals(p.getStatus())) throw new RuntimeException("Purchase not DRAFT");

        PurchaseItem it = itemRepo.findById(itemId).orElseThrow();
        if (!it.getPurchaseId().equals(purchaseId)) throw new RuntimeException("Item not belong to purchase");

        itemRepo.delete(it);
        recalcTotal(purchaseId);
    }

    private void recalcTotal(Long purchaseId){
        BigDecimal total = itemRepo.findByPurchaseId(purchaseId).stream()
                .map(PurchaseItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Purchase p = purchaseRepo.findById(purchaseId).orElseThrow();
        p.setTotal(total);
        purchaseRepo.save(p);
    }

    /* =======================
       RECEIVE (DRAFT -> RECEIVED + call inventory inbound)
       ======================= */

    @Transactional
    public ReceiveResponse receive(Long purchaseId, String bearerToken, Long userId) {

        Purchase p = purchaseRepo.findById(purchaseId).orElseThrow();
        if (!"DRAFT".equals(p.getStatus())) throw new RuntimeException("Purchase not DRAFT");

        List<PurchaseItem> items = itemRepo.findByPurchaseId(purchaseId);
        if (items.isEmpty()) throw new RuntimeException("No items");

        List<InboundRequest.Item> inboundItems = new ArrayList<>();
        for (var it : items) {
            int conversionFactor = it.getConversionFactor() != null && it.getConversionFactor() > 0
                    ? it.getConversionFactor()
                    : 1;
            BigDecimal baseUnitImportPrice = (it.getImportPrice() == null ? BigDecimal.ZERO : it.getImportPrice())
                    .divide(BigDecimal.valueOf(conversionFactor), 2, RoundingMode.HALF_UP);
            int baseQty = Math.multiplyExact(it.getQty(), conversionFactor);
            inboundItems.add(new InboundRequest.Item(
                    it.getMedicineId(),
                    it.getMedicineName(),
                    it.getLotNumber(),
                    it.getExpiryDate(),
                    baseUnitImportPrice,
                    baseQty
            ));
        }

        InboundRequest inboundReq = new InboundRequest("PURCHASE", p.getCode(), userId, inboundItems);
        InboundResponse inboundResp = inventoryClient.inbound(bearerToken, inboundReq);

        p.setStatus("RECEIVED");
        p.setReceivedAt(LocalDateTime.now());
        purchaseRepo.save(p);

        return new ReceiveResponse(p.getCode(), p.getStatus(), inboundResp);
    }
}
