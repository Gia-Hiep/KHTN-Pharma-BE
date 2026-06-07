package com.pharmacy.sales_service.service;

import com.pharmacy.sales_service.client.CatalogClient;
import com.pharmacy.sales_service.client.CustomerClient;
import com.pharmacy.sales_service.client.InventoryClient;
import com.pharmacy.sales_service.dto.*;
import com.pharmacy.sales_service.entity.*;
import com.pharmacy.sales_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final InvoiceRepo invoiceRepo;
    private final InvoiceItemRepo itemRepo;
    private final InvoiceLotAllocationRepo allocRepo;
    private final PaymentRepo paymentRepo;

    private final InventoryClient inventoryClient;
    private final CustomerClient customerClient;
    private final CatalogClient catalogClient;

    /* =======================
       CREATE INVOICE
       ======================= */
    @Transactional
    public Invoice createInvoice(String code, Long cashierId, String bearerToken, String customerPhone, String customerName, Long existingCustomerId){
        Long customerId = null;

        // Priority 1: use existing customer ID if provided
        if (existingCustomerId != null) {
            customerId = existingCustomerId;
        }
        // Priority 2: look up or create by phone
        else if (customerPhone != null && !customerPhone.isBlank()) {
            var c = customerClient.getByPhone(bearerToken, customerPhone);

            if (c == null) {
                String name = (customerName == null || customerName.isBlank()) ? "khach le" : customerName;
                c = customerClient.create(
                        bearerToken,
                        new CustomerClient.CreateCustomerDto(
                                name,
                                customerPhone,
                                null, null, null,
                                "created from sales-service"
                        )
                );
            }
            customerId = c.id();
        }

        if (code == null || code.isBlank()) {
            String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randPart = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
            code = "INV-" + datePart + "-" + randPart;
        }

        Invoice inv = new Invoice();
        inv.setCode(code);
        inv.setCashierId(cashierId);
        inv.setCustomerId(customerId);
        inv.setStatus("DRAFT");
        inv.setPaymentStatus("UNPAID");
        inv.setSubtotal(BigDecimal.ZERO);
        inv.setDiscount(BigDecimal.ZERO);
        inv.setTotal(BigDecimal.ZERO);
        inv.setCreatedAt(LocalDateTime.now());
        inv.setUpdatedAt(LocalDateTime.now());
        return invoiceRepo.save(inv);
    }

    /* =======================
       ADD ITEM (price from catalog)
       ======================= */
    @Transactional
    public InvoiceItem addItem(Long invoiceId, AddItemRequest req, String bearerToken){
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if (!"DRAFT".equals(inv.getStatus())) throw new RuntimeException("Invoice not DRAFT");
        if (req.qty() <= 0) throw new RuntimeException("qty must be > 0");

        var med = catalogClient.getMedicineById(bearerToken, req.medicineId());
        if (med == null) throw new RuntimeException("Medicine not found");
        if (med.salePrice() == null) throw new RuntimeException("Medicine missing salePrice");

        String requestedSaleMode = resolveSaleMode(req.saleMode());
        String unitCode = req.unitCode();
        String unitLabel = req.unitLabel();
        Integer conversionFactor = resolveConversionFactor(req.conversionFactor());
        BigDecimal unitPrice = med.salePrice();
        var priceResult = catalogClient.calculatePrice(
                bearerToken,
                req.medicineId(),
                unitCode,
                requestedSaleMode,
                req.qty()
        );
        if (priceResult != null) {
            if (priceResult.unitPrice() != null) {
                unitPrice = priceResult.unitPrice();
            }
            requestedSaleMode = resolveSaleMode(priceResult.saleMode() != null ? priceResult.saleMode() : priceResult.tierCode());
            if (priceResult.unitCode() != null && !priceResult.unitCode().isBlank()) {
                unitCode = priceResult.unitCode();
            }
            if (priceResult.unitLabel() != null && !priceResult.unitLabel().isBlank()) {
                unitLabel = priceResult.unitLabel();
            }
            conversionFactor = resolveConversionFactor(priceResult.conversionFactor());
        }
        BigDecimal line = unitPrice.multiply(BigDecimal.valueOf(req.qty()));

        InvoiceItem it = new InvoiceItem();
        it.setInvoiceId(invoiceId);
        it.setMedicineId(req.medicineId());

        // ✅ NEW: lưu tên thuốc từ catalog (cột name)
        String medName = med.name();
        if (medName == null || medName.isBlank()) medName = "#" + req.medicineId();
        it.setMedicineName(medName);

        it.setUnitCode(unitCode);
        it.setUnitLabel(unitLabel);
        it.setConversionFactor(conversionFactor);
        it.setSaleMode(requestedSaleMode);
        it.setQty(req.qty());
        it.setUnitPrice(unitPrice);
        it.setLineTotal(line);

        itemRepo.save(it);

        recalcTotals(inv);
        return it;
    }

    @Transactional
    public void removeItem(Long invoiceId, Long itemId) {
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if (!"DRAFT".equals(inv.getStatus())) throw new RuntimeException("Invoice not DRAFT");

        InvoiceItem it = itemRepo.findById(itemId).orElseThrow();
        if (!it.getInvoiceId().equals(invoiceId)) throw new RuntimeException("Item does not belong to invoice");

        itemRepo.delete(it);
        itemRepo.flush(); // Ensure deletion before recalc
        recalcTotals(inv);
    }

    @Transactional
    public InvoiceItem updateItemQty(Long invoiceId, Long itemId, int newQty, String bearerToken) {
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if (!"DRAFT".equals(inv.getStatus())) throw new RuntimeException("Invoice not DRAFT");
        if (newQty <= 0) throw new RuntimeException("qty must be > 0");

        InvoiceItem it = itemRepo.findById(itemId).orElseThrow();
        if (!it.getInvoiceId().equals(invoiceId)) throw new RuntimeException("Item does not belong to invoice");

        var priceResult = catalogClient.calculatePrice(
                bearerToken,
                it.getMedicineId(),
                it.getUnitCode(),
                resolveSaleMode(it.getSaleMode()),
                newQty
        );

        if (priceResult != null) {
            if (priceResult.unitPrice() != null) {
                it.setUnitPrice(priceResult.unitPrice());
            }
            if (priceResult.unitCode() != null && !priceResult.unitCode().isBlank()) {
                it.setUnitCode(priceResult.unitCode());
            }
            if (priceResult.unitLabel() != null && !priceResult.unitLabel().isBlank()) {
                it.setUnitLabel(priceResult.unitLabel());
            }
            it.setConversionFactor(resolveConversionFactor(priceResult.conversionFactor()));
            it.setSaleMode(resolveSaleMode(priceResult.saleMode() != null ? priceResult.saleMode() : priceResult.tierCode()));
        }

        it.setQty(newQty);
        it.setLineTotal(it.getUnitPrice().multiply(BigDecimal.valueOf(newQty)));
        itemRepo.save(it);
        
        recalcTotals(inv);
        return it;
    }

    private void recalcTotals(Invoice inv){
        BigDecimal subtotal = itemRepo.findByInvoiceId(inv.getId()).stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        inv.setSubtotal(subtotal);
        inv.setTotal(subtotal.subtract(inv.getDiscount() == null ? BigDecimal.ZERO : inv.getDiscount()));
        inv.setUpdatedAt(LocalDateTime.now());
        invoiceRepo.save(inv);
    }

    /* =======================
       CHECKOUT -> reserve inventory + allocations + status WAIT_PAYMENT
       ======================= */
    @Transactional
    public CheckoutResponse checkout(Long invoiceId, String bearerToken){
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if ("WAIT_PAYMENT".equals(inv.getStatus())) {
            throw new RuntimeException("Invoice already checkout, please pay or cancel");
        }
        if (!"DRAFT".equals(inv.getStatus())) {
            throw new RuntimeException("Invoice status=" + inv.getStatus() + " cannot checkout");
        }

        var items = itemRepo.findByInvoiceId(invoiceId);
        if (items.isEmpty()) throw new RuntimeException("No items");

        List<InventoryReserveRequest.Item> reqItems = new ArrayList<>();
        for (var it : items){
            reqItems.add(new InventoryReserveRequest.Item(it.getMedicineId(), toBaseQty(it)));
        }

        InventoryReserveRequest req = new InventoryReserveRequest("INVOICE", inv.getCode(), "FEFO", reqItems);
        InventoryReserveResponse resp = inventoryClient.reserve(bearerToken, req);

        allocRepo.deleteByInvoiceId(invoiceId);

        Map<Long, List<InvoiceItem>> itemsByMedicine = new LinkedHashMap<>();
        Map<Long, Map<Long, Integer>> remainingBaseQtyByItem = new LinkedHashMap<>();
        for (InvoiceItem item : items) {
            itemsByMedicine.computeIfAbsent(item.getMedicineId(), ignored -> new ArrayList<>()).add(item);
            remainingBaseQtyByItem
                    .computeIfAbsent(item.getMedicineId(), ignored -> new LinkedHashMap<>())
                    .put(item.getId(), toBaseQty(item));
        }

        for (var reservation : resp.reservations()){
            List<InvoiceItem> targets = itemsByMedicine.getOrDefault(reservation.medicineId(), List.of());
            Map<Long, Integer> remainingByItem = remainingBaseQtyByItem.getOrDefault(reservation.medicineId(), Map.of());
            int remainingReservationQty = reservation.qty();

            for (InvoiceItem target : targets) {
                if (remainingReservationQty <= 0) break;
                int remainingItemQty = remainingByItem.getOrDefault(target.getId(), 0);
                if (remainingItemQty <= 0) continue;

                int allocatedQty = Math.min(remainingReservationQty, remainingItemQty);
                InvoiceLotAllocation allocation = new InvoiceLotAllocation();
                allocation.setInvoiceItemId(target.getId());
                allocation.setLotId(reservation.lotId());
                allocation.setQty(allocatedQty);
                allocRepo.save(allocation);

                remainingReservationQty -= allocatedQty;
                remainingByItem.put(target.getId(), remainingItemQty - allocatedQty);
            }
        }

        inv.setStatus("WAIT_PAYMENT");
        inv.setUpdatedAt(LocalDateTime.now());
        invoiceRepo.save(inv);

        return new CheckoutResponse(inv.getCode(), resp.reservations());
    }

    /* =======================
       PAY -> commit inventory + payment + status PAID
       ======================= */
    @Transactional
    public void pay(Long invoiceId, PayRequest req, String bearerToken){
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if (!"WAIT_PAYMENT".equals(inv.getStatus())) throw new RuntimeException("Invoice not WAIT_PAYMENT");

        inventoryClient.commit(bearerToken, "INVOICE", inv.getCode());

        Payment p = new Payment();
        p.setInvoiceId(invoiceId);
        p.setAmount(req.amount());
        p.setPaymentMethod(req.paymentMethod());
        p.setTransactionId(req.transactionId());
        p.setStatus("SUCCESS");
        p.setPaidAt(LocalDateTime.now());
        paymentRepo.save(p);

        inv.setPaymentStatus("PAID");
        inv.setStatus("PAID");
        inv.setUpdatedAt(LocalDateTime.now());
        invoiceRepo.save(inv);
    }

    /* =======================
       READ FOR FE
       ======================= */
    @Transactional(readOnly = true)
    public List<Invoice> listInvoices(String status, String paymentStatus, Long cashierId,
                                      LocalDateTime from, LocalDateTime to){
        return invoiceRepo.search(status, paymentStatus, cashierId, from, to);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoice(Long invoiceId){
        return invoiceRepo.findById(invoiceId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<InvoiceItem> getItems(Long invoiceId){
        return itemRepo.findByInvoiceId(invoiceId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPayments(Long invoiceId){
        return paymentRepo.findByInvoiceId(invoiceId);
    }

    /* =======================
       CANCEL + RELEASE
       ======================= */
    @Transactional
    public Invoice cancelInvoice(Long invoiceId, String bearerToken, Long userId, String reason){
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();

        // only allow cancel if not PAID
        if ("PAID".equals(inv.getStatus())) throw new RuntimeException("Cannot cancel a PAID invoice");

        // if already reserved, must release
        if ("WAIT_PAYMENT".equals(inv.getStatus())) {
            inventoryClient.release(bearerToken, "INVOICE", inv.getCode());
        }

        inv.setStatus("CANCELLED");
        inv.setPaymentStatus("UNPAID");
        if (reason != null && !reason.isBlank()){
            inv.setNotes(reason);
        }
        inv.setUpdatedAt(LocalDateTime.now());
        return invoiceRepo.save(inv);
    }

    private String resolveSaleMode(String saleMode) {
        return saleMode != null && !saleMode.isBlank() ? saleMode : "RETAIL";
    }

    private Integer resolveConversionFactor(Integer conversionFactor) {
        return conversionFactor != null && conversionFactor > 0 ? conversionFactor : 1;
    }

    private int toBaseQty(InvoiceItem item) {
        return Math.multiplyExact(item.getQty(), resolveConversionFactor(item.getConversionFactor()));
    }

}
