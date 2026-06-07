package com.pharmacy.inventory_service.service;

import com.pharmacy.inventory_service.dto.*;
import com.pharmacy.inventory_service.entity.*;
import com.pharmacy.inventory_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StockLotRepo stockLotRepo;
    private final ReservationRepo reservationRepo;
    private final ReservationItemRepo reservationItemRepo;
    private final StockTransactionRepo txRepo;
    private final StockAlertRuleRepo ruleRepo;

    /* =======================
       INBOUND
       ======================= */
    @Transactional
    public InboundResponse inbound(InboundRequest req) {

        if (req.items() == null || req.items().isEmpty()) throw new RuntimeException("No items");
        if (req.performedBy() == null) throw new RuntimeException("performedBy is required");

        List<InboundResponse.Result> results = new ArrayList<>();

        for (var it : req.items()) {
            if (it.qty() <= 0) throw new RuntimeException("qty must be > 0");
            if (it.lotNumber() == null || it.lotNumber().isBlank()) throw new RuntimeException("lotNumber required");
            if (it.expiryDate() == null) throw new RuntimeException("expiryDate required");

            StockLot lot = stockLotRepo
                    .findByMedicineIdAndLotNumberAndExpiryDate(it.medicineId(), it.lotNumber(), it.expiryDate())
                    .orElseGet(() -> {
                        StockLot nl = new StockLot();
                        nl.setMedicineId(it.medicineId());
                        nl.setMedicineName(it.medicineName());
                        nl.setLotNumber(it.lotNumber());
                        nl.setExpiryDate(it.expiryDate());
                        nl.setImportPrice(it.importPrice() == null ? BigDecimal.ZERO : it.importPrice());
                        nl.setQtyOnHand(0);
                        nl.setQtyReserved(0);
                        nl.setCreatedAt(LocalDateTime.now());
                        return nl;
                    });

            lot.setImportPrice(it.importPrice() == null ? lot.getImportPrice() : it.importPrice());
            if (it.medicineName() != null) lot.setMedicineName(it.medicineName());
            lot.setQtyOnHand(lot.getQtyOnHand() + it.qty());
            StockLot savedLot = stockLotRepo.save(lot);

            StockTransaction tx = new StockTransaction();
            tx.setType("IN");
            tx.setRefType(req.refType());
            tx.setRefId(req.refId());
            tx.setMedicineId(it.medicineId());
            tx.setLotId(savedLot.getId());
            tx.setQty(it.qty());
            tx.setPerformedBy(req.performedBy());
            tx.setNote("inbound from purchase receive");
            tx.setCreatedAt(LocalDateTime.now());
            txRepo.save(tx);

            results.add(new InboundResponse.Result(
                    it.medicineId(),
                    savedLot.getId(),
                    savedLot.getLotNumber(),
                    it.qty(),
                    savedLot.getQtyOnHand()
            ));
        }

        return new InboundResponse(req.refType(), req.refId(), results);
    }

    /* =======================
       RESERVE / COMMIT / RELEASE
       ======================= */
    @Transactional
    public ReserveResponse reserve(ReserveRequest req, Long userId){
        Reservation res = reservationRepo.findByRefTypeAndRefId(req.refType(), req.refId())
                .orElseGet(() -> {
                    Reservation r = new Reservation();
                    r.setRefType(req.refType());
                    r.setRefId(req.refId());
                    r.setStatus("ACTIVE");
                    r.setCreatedBy(userId);
                    r.setCreatedAt(LocalDateTime.now());
                    return reservationRepo.save(r);
                });

        if (!"ACTIVE".equals(res.getStatus())) {
            throw new RuntimeException("Reservation not ACTIVE");
        }

        // ── Idempotent: clear previous reservation items if any ──
        var existingItems = reservationItemRepo.findByReservationIdForUpdate(res.getId());
        if (!existingItems.isEmpty()) {
            for (var ei : existingItems) {
                StockLot lot = stockLotRepo.findByIdForUpdate(ei.getLotId()).orElse(null);
                if (lot != null) {
                    lot.setQtyReserved(lot.getQtyReserved() - ei.getQty());
                    stockLotRepo.save(lot);
                }
            }
            reservationItemRepo.deleteAll(existingItems);
        }

        List<ReserveResponse.Reserved> out = new ArrayList<>();

        for (var item : req.items()){
            int need = item.qty();
            var lots = stockLotRepo.findAvailableLotsFefoForUpdate(item.medicineId());

            for (var lot : lots){
                int free = lot.getQtyOnHand() - lot.getQtyReserved();
                if (free <= 0) continue;

                int take = Math.min(free, need);
                lot.setQtyReserved(lot.getQtyReserved() + take);
                stockLotRepo.save(lot);

                ReservationItem ri = new ReservationItem();
                ri.setReservationId(res.getId());
                ri.setMedicineId(item.medicineId());
                ri.setLotId(lot.getId());
                ri.setQty(take);
                reservationItemRepo.save(ri);

                txRepo.save(StockTransaction.of("RESERVE", req.refType(), req.refId(), item.medicineId(), lot.getId(), take, userId, "reserve"));

                out.add(new ReserveResponse.Reserved(item.medicineId(), lot.getId(), take));
                need -= take;
                if (need == 0) break;
            }

            if (need > 0) throw new RuntimeException("Not enough stock for medicineId=" + item.medicineId());
        }

        return new ReserveResponse(out);
    }

    @Transactional
    public void commit(String refType, String refId, Long userId){
        Reservation res = reservationRepo.findByRefTypeAndRefId(refType, refId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (!"ACTIVE".equals(res.getStatus())) return;

        var items = reservationItemRepo.findByReservationIdForUpdate(res.getId());
        for (var it : items){
            StockLot lot = stockLotRepo.findByIdForUpdate(it.getLotId())
                    .orElseThrow(() -> new RuntimeException("Lot not found"));

            if (lot.getQtyReserved() < it.getQty()) throw new RuntimeException("Reserved inconsistency");
            if (lot.getQtyOnHand() < it.getQty()) throw new RuntimeException("Stock inconsistency");

            lot.setQtyReserved(lot.getQtyReserved() - it.getQty());
            lot.setQtyOnHand(lot.getQtyOnHand() - it.getQty());
            stockLotRepo.save(lot);

            txRepo.save(StockTransaction.of("COMMIT", refType, refId, it.getMedicineId(), it.getLotId(), it.getQty(), userId, "commit"));
        }

        res.setStatus("COMMITTED");
        reservationRepo.save(res);
    }

    @Transactional
    public void release(String refType, String refId, Long userId){
        Reservation res = reservationRepo.findByRefTypeAndRefId(refType, refId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (!"ACTIVE".equals(res.getStatus())) return;

        var items = reservationItemRepo.findByReservationIdForUpdate(res.getId());
        for (var it : items){
            StockLot lot = stockLotRepo.findByIdForUpdate(it.getLotId())
                    .orElseThrow(() -> new RuntimeException("Lot not found"));

            lot.setQtyReserved(lot.getQtyReserved() - it.getQty());
            stockLotRepo.save(lot);

            txRepo.save(StockTransaction.of("RELEASE", refType, refId, it.getMedicineId(), it.getLotId(), it.getQty(), userId, "release"));
        }

        res.setStatus("RELEASED");
        reservationRepo.save(res);
    }

    /**
     * Partially release reservation items — reduce reserved qty per medicine.
     * Used during PICKING when pharmacist reports insufficient stock.
     * Atomic: reduces ReservationItem.qty and StockLot.qtyReserved in one transaction.
     */
    @Transactional
    public void adjustReservation(com.pharmacy.inventory_service.dto.AdjustReservationRequest req, Long userId) {
        Reservation res = reservationRepo.findByRefTypeAndRefId(req.refType(), req.refId())
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (!"ACTIVE".equals(res.getStatus())) {
            throw new RuntimeException("Reservation is not ACTIVE, cannot adjust");
        }

        var items = reservationItemRepo.findByReservationIdForUpdate(res.getId());

        for (var adj : req.adjustments()) {
            // Find reservation items for this medicineId (may span multiple lots)
            var matchingItems = items.stream()
                    .filter(it -> it.getMedicineId().equals(adj.medicineId()))
                    .toList();

            int remaining = adj.reduceQty();
            for (var it : matchingItems) {
                if (remaining <= 0) break;

                int reduce = Math.min(remaining, it.getQty());
                StockLot lot = stockLotRepo.findByIdForUpdate(it.getLotId())
                        .orElseThrow(() -> new RuntimeException("Lot not found"));

                lot.setQtyReserved(lot.getQtyReserved() - reduce);
                stockLotRepo.save(lot);

                it.setQty(it.getQty() - reduce);
                if (it.getQty() <= 0) {
                    reservationItemRepo.delete(it);
                } else {
                    reservationItemRepo.save(it);
                }

                txRepo.save(StockTransaction.of("PARTIAL_RELEASE", req.refType(), req.refId(),
                        adj.medicineId(), it.getLotId(), reduce, userId,
                        "partial release: -" + reduce));

                remaining -= reduce;
            }
        }
    }

    /**
     * Return stock for cancelled orders that were already committed (PICKING/PACKING).
     * Finds COMMIT transactions by refType/refId and adds qty back to stock lots.
     */
    @Transactional
    public void returnStock(String refType, String refId, Long userId) {
        List<StockTransaction> commits = txRepo.search(refType, refId, null, "COMMIT", null, null);
        if (commits.isEmpty()) return; // Nothing committed yet

        for (var tx : commits) {
            StockLot lot = stockLotRepo.findByIdForUpdate(tx.getLotId())
                    .orElse(null);
            if (lot == null) continue;

            lot.setQtyOnHand(lot.getQtyOnHand() + tx.getQty());
            stockLotRepo.save(lot);

            txRepo.save(StockTransaction.of("RETURN", refType, refId,
                    tx.getMedicineId(), tx.getLotId(), tx.getQty(), userId, "cancel-return"));
        }
    }

    /* =======================
       VIEW STOCK (FE)
       ======================= */
    @Transactional(readOnly = true)
    public List<StockLotDto> listLots(Long medicineId, LocalDate expBefore, boolean onlyAvailable){
        var lots = stockLotRepo.findLots(medicineId, expBefore);

        List<StockLotDto> out = new ArrayList<>();
        for (var l : lots){
            int available = l.getQtyOnHand() - l.getQtyReserved();
            if (onlyAvailable && available <= 0) continue;

            out.add(new StockLotDto(
                    l.getId(),
                    l.getMedicineId(),
                    l.getMedicineName(),
                    l.getLotNumber(),
                    l.getExpiryDate(),
                    l.getImportPrice(),
                    l.getQtyOnHand(),
                    l.getQtyReserved(),
                    available,
                    l.getCreatedAt()
            ));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public StockLotDto getLot(Long lotId){
        StockLot l = stockLotRepo.findById(lotId).orElseThrow();
        int available = l.getQtyOnHand() - l.getQtyReserved();

        return new StockLotDto(
                l.getId(),
                l.getMedicineId(),
                l.getMedicineName(),
                l.getLotNumber(),
                l.getExpiryDate(),
                l.getImportPrice(),
                l.getQtyOnHand(),
                l.getQtyReserved(),
                available,
                l.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<StockSummaryDto> summary(Long medicineId){
        List<Object[]> rows = (medicineId == null)
                ? stockLotRepo.summaryAll()
                : stockLotRepo.summaryOne(medicineId);

        List<StockSummaryDto> out = new ArrayList<>();
        for (var r : rows){
            Long medId = (Long) r[0];
            String medName = (String) r[1];
            Number onHand = (Number) r[2];
            Number reserved = (Number) r[3];

            long oh = onHand == null ? 0 : onHand.longValue();
            long rs = reserved == null ? 0 : reserved.longValue();
            out.add(new StockSummaryDto(medId, medName, oh, rs, oh - rs));
        }
        return out;
    }

    /* =======================
       ALERTS
       ======================= */
    @Transactional(readOnly = true)
    public List<LowStockAlertDto> lowStockAlerts(){
        Map<Long, Integer> ruleMap = new HashMap<>();
        for (var r : ruleRepo.findAll()){
            ruleMap.put(r.getMedicineId(), r.getMinStockLevel());
        }

        List<LowStockAlertDto> out = new ArrayList<>();
        for (var row : stockLotRepo.summaryAll()){
            Long medId = (Long) row[0];
            String medName = (String) row[1];
            long onHand = row[2] == null ? 0 : ((Number)row[2]).longValue();
            long reserved = row[3] == null ? 0 : ((Number)row[3]).longValue();
            long available = onHand - reserved;

            int min = ruleMap.getOrDefault(medId, 10);
            if (available < min){
                out.add(new LowStockAlertDto(medId, medName, available, min));
            }
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<ExpiryAlertDto> expiryAlerts(LocalDate before){
        if (before == null) before = LocalDate.now().plusDays(30);

        var lots = stockLotRepo.findLots(null, before);

        List<ExpiryAlertDto> out = new ArrayList<>();
        for (var l : lots){
            int available = l.getQtyOnHand() - l.getQtyReserved();
            if (available <= 0) continue;

            out.add(new ExpiryAlertDto(
                    l.getId(),
                    l.getMedicineId(),
                    l.getMedicineName(),
                    l.getLotNumber(),
                    l.getExpiryDate(),
                    available
            ));
        }
        return out;
    }

    /* =======================
       TRANSACTIONS SEARCH
       ======================= */
    @Transactional(readOnly = true)
    public List<StockTransaction> searchTx(String refType, String refId, Long medicineId, String type,
                                           LocalDateTime from, LocalDateTime to){
        return txRepo.search(refType, refId, medicineId, type, from, to);
    }

    /* =======================
       ADMIN ACTIONS
       ======================= */
    @Transactional
    public void adjust(AdjustRequest req, Long userId){

        StockLot lot = stockLotRepo.findByIdForUpdate(req.lotId())
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        Integer ch = req.qtyChange();
        if (ch == null || ch == 0) return;
        int newOnHand = lot.getQtyOnHand() + ch;

        if (newOnHand < 0) throw new RuntimeException("qty_on_hand cannot be negative");
        if (newOnHand < lot.getQtyReserved()) throw new RuntimeException("cannot drop below reserved");

        lot.setQtyOnHand(newOnHand);
        stockLotRepo.save(lot);

        txRepo.save(StockTransaction.of(
                "ADJUST",
                null, null,
                lot.getMedicineId(),
                lot.getId(),
                Math.abs(req.qtyChange()),
                userId,
                req.reason()
        ));
    }

    @Transactional
    public void markDamaged(LotActionRequest req, Long userId){
        lotOut("DAMAGED", req, userId);
    }

    @Transactional
    public void markExpired(LotActionRequest req, Long userId){
        lotOut("EXPIRED", req, userId);
    }

    private void lotOut(String type, LotActionRequest req, Long userId){
        if (req.qty() <= 0) throw new RuntimeException("qty must be > 0");

        StockLot lot = stockLotRepo.findByIdForUpdate(req.lotId())
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        int available = lot.getQtyOnHand() - lot.getQtyReserved();
        if (available < req.qty()) throw new RuntimeException("Not enough available in lot");

        lot.setQtyOnHand(lot.getQtyOnHand() - req.qty());
        stockLotRepo.save(lot);

        txRepo.save(StockTransaction.of(
                type,
                null, null,
                lot.getMedicineId(),
                lot.getId(),
                req.qty(),
                userId,
                req.reason()
        ));
    }

    /* =======================
       REPAIR: Fix corrupted qtyReserved
       Recalculates from actual ACTIVE reservation items
       ======================= */
    @Transactional
    public Map<String, Object> repairReservedQuantities() {
        // Step 1: Reset ALL lots to qtyReserved = 0
        List<StockLot> allLots = stockLotRepo.findAll();
        for (StockLot lot : allLots) {
            lot.setQtyReserved(0);
        }
        stockLotRepo.saveAll(allLots);

        // Step 2: Find all ACTIVE reservations
        List<Reservation> activeReservations = reservationRepo.findAll().stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .toList();

        // Step 3: For each active reservation, sum up items by lotId
        int totalItems = 0;
        for (Reservation res : activeReservations) {
            List<ReservationItem> items = reservationItemRepo.findByReservationId(res.getId());
            for (ReservationItem item : items) {
                StockLot lot = stockLotRepo.findById(item.getLotId()).orElse(null);
                if (lot != null) {
                    lot.setQtyReserved(lot.getQtyReserved() + item.getQty());
                    stockLotRepo.save(lot);
                    totalItems++;
                }
            }
        }

        return Map.of(
                "lotsReset", allLots.size(),
                "activeReservations", activeReservations.size(),
                "reservationItemsProcessed", totalItems
        );
    }
}
