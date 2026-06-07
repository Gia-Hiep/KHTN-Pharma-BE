package com.pharmacy.sales_service.service;

import com.pharmacy.sales_service.client.AuthClient;
import com.pharmacy.sales_service.client.CatalogClient;
import com.pharmacy.sales_service.client.InventoryClient;
import com.pharmacy.sales_service.client.NotificationClient;
import com.pharmacy.sales_service.dto.*;
import com.pharmacy.sales_service.entity.*;
import com.pharmacy.sales_service.repository.BuyerOrderRepo;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BuyerOrderService {

    private final BuyerOrderRepo repo;
    private final InventoryClient inventoryClient;
    private final CatalogClient catalogClient;
    private final AuthClient authClient;
    private final NotificationClient notificationClient;
    private final WalletService walletService;
    private final StripeService stripeService;

    /**
     * State machine: valid transitions.
     * Reject/cancel handled separately with dedicated methods.
     */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PENDING_APPROVAL, Set.of(OrderStatus.CONFIRMED, OrderStatus.REJECTED),
            OrderStatus.CONFIRMED,        Set.of(OrderStatus.PICKING, OrderStatus.REJECTED),
            OrderStatus.PICKING,          Set.of(OrderStatus.PACKING, OrderStatus.REJECTED),
            OrderStatus.PACKING,          Set.of(OrderStatus.SHIPPING, OrderStatus.REJECTED),
            OrderStatus.SHIPPING,         Set.of(OrderStatus.DELIVERED, OrderStatus.REJECTED),
            OrderStatus.DELIVERED,        Set.of(OrderStatus.RETURNED)
    );

    /* ═══════════════════════════════════════════════════════════════════════
       BUYER — tạo đơn, xem đơn, hủy đơn
       ═══════════════════════════════════════════════════════════════════════ */

    @Transactional
    public BuyerOrderResponse create(Long buyerId, CreateOrderRequest req, String bearerToken) {
        // Parse payment method
        PaymentMethod pm = PaymentMethod.COD;
        if (req.paymentMethod() != null) {
            try { pm = PaymentMethod.valueOf(req.paymentMethod().toUpperCase()); }
            catch (Exception ignored) { /* default COD */ }
        }

        PaymentStatus requestedPaymentStatus = parsePaymentStatus(req.paymentStatus());

        BuyerOrder order = BuyerOrder.builder()
                .buyerId(buyerId)
                .buyerName(req.buyerName())
                .shippingAddress(req.shippingAddress())
                .paymentMethod(pm)
                .paymentStatus(requestedPaymentStatus)
                .notes(req.notes())
                .couponCode(req.couponCode())
                .status(OrderStatus.PENDING_APPROVAL)
                .build();

        if (req.items() != null) {
            for (var it : req.items()) {
                String saleMode = resolveSaleMode(it.priceTier(), it.saleMode());
                String unitCode = it.unitCode();
                String unitLabel = it.unitLabel();
                Integer conversionFactor = resolveConversionFactor(it.conversionFactor());
                BigDecimal unitPrice = it.unitPrice() != null ? it.unitPrice() : BigDecimal.ZERO;
                try {
                    var priceResult = catalogClient.calculatePrice(
                            bearerToken, it.medicineId(),
                            unitCode,
                            saleMode,
                            it.qty());
                    if (priceResult != null) {
                        if (priceResult.unitPrice() != null) {
                            unitPrice = priceResult.unitPrice();
                        }
                        saleMode = resolveSaleMode(priceResult.tierCode(), priceResult.saleMode());
                        if (priceResult.unitCode() != null && !priceResult.unitCode().isBlank()) {
                            unitCode = priceResult.unitCode();
                        }
                        if (priceResult.unitLabel() != null && !priceResult.unitLabel().isBlank()) {
                            unitLabel = priceResult.unitLabel();
                        }
                        conversionFactor = resolveConversionFactor(priceResult.conversionFactor());
                    }
                } catch (Exception ignored) { /* fallback to FE price */ }

                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(it.qty()));
                BuyerOrderItem item = BuyerOrderItem.builder()
                        .order(order).medicineId(it.medicineId()).medicineName(it.medicineName())
                        .qty(it.qty()).originalQty(it.qty())
                        .unitCode(unitCode).unitLabel(unitLabel).conversionFactor(conversionFactor)
                        .unitPrice(unitPrice).lineTotal(lineTotal).priceTier(saleMode)
                        .fulfilled(false)
                        .build();
                order.getItems().add(item);
            }
        }
        recalcTotals(order);

        if (pm == PaymentMethod.STRIPE && requestedPaymentStatus == PaymentStatus.PAID) {
            verifyStripePayment(req.stripePaymentIntentId(), order.getTotal());
            order.setStripePaymentIntentId(req.stripePaymentIntentId());
            order.setPaymentTransactionId(req.stripePaymentIntentId());
        }

        BuyerOrder saved = repo.save(order);

        // WALLET: trừ ví ngay khi tạo đơn
        if (pm == PaymentMethod.WALLET) {
            walletService.pay(buyerId, saved.getTotal(), saved.getId());
            saved.setPaymentStatus(PaymentStatus.PAID);
            repo.save(saved);
        }

        return BuyerOrderResponse.from(saved);
    }

    private void verifyStripePayment(String paymentIntentId, BigDecimal expectedTotal) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Thiếu mã giao dịch Stripe");
        }

        try {
            PaymentIntent pi = stripeService.retrieveIntent(paymentIntentId);
            long expectedAmount = expectedTotal != null ? expectedTotal.longValue() : 0L;

            if (!"succeeded".equalsIgnoreCase(pi.getStatus())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Thanh toán Stripe chưa thành công");
            }
            if (!"vnd".equalsIgnoreCase(pi.getCurrency())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Đơn vị tiền tệ Stripe không hợp lệ");
            }
            if (pi.getAmount() == null || pi.getAmount() != expectedAmount) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Số tiền Stripe không khớp tổng đơn hàng");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Không xác minh được thanh toán Stripe: " + e.getMessage());
        }
    }

    private String resolveSaleMode(String priceTier, String saleMode) {
        if (saleMode != null && !saleMode.isBlank()) return saleMode;
        if (priceTier != null && !priceTier.isBlank()) return priceTier;
        return "RETAIL";
    }

    private Integer resolveConversionFactor(Integer conversionFactor) {
        return conversionFactor != null && conversionFactor > 0 ? conversionFactor : 1;
    }

    private int toBaseQty(BuyerOrderItem item) {
        return Math.multiplyExact(
                item.getQty() != null && item.getQty() > 0 ? item.getQty() : 0,
                resolveConversionFactor(item.getConversionFactor())
        );
    }

    @Transactional(readOnly = true)
    public List<BuyerOrderResponse> myOrders(Long buyerId) {
        return repo.findByBuyerIdOrderByCreatedAtDesc(buyerId).stream()
                .map(BuyerOrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BuyerOrderResponse myOrderDetail(Long buyerId, Long orderId) {
        BuyerOrder order = repo.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy hoặc không có quyền truy cập"));
        return BuyerOrderResponse.from(order);
    }

    /** BUYER hủy đơn — chỉ khi PENDING_APPROVAL */
    @Transactional
    public BuyerOrderResponse cancelOrder(Long buyerId, Long orderId, String bearerToken) {
        BuyerOrder order = repo.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy hoặc không có quyền"));
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Chỉ có thể hủy đơn ở trạng thái Chờ duyệt");
        }
        order.setStatus(OrderStatus.REJECTED);
        order.setRejectionReason("BUYER_CANCELLED");
        try { inventoryClient.release(bearerToken, "BUYER_ORDER", String.valueOf(orderId)); }
        catch (Exception e) { /* no reservation to release */ }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            PaymentMethod pm = order.getPaymentMethod();
            if (pm == PaymentMethod.BANK_TRANSFER || pm == PaymentMethod.STRIPE
                    || pm == PaymentMethod.WALLET || pm == PaymentMethod.ONLINE) {
                walletService.refund(order.getBuyerId(), order.getTotal(), orderId,
                        "Hoan tien don #" + orderId + " (buyer tu huy)");
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            }
        }

        return BuyerOrderResponse.from(repo.save(order));
    }

    /* ═══════════════════════════════════════════════════════════════════════
       PHARMACIST — danh sách, chi tiết, backfill
       ═══════════════════════════════════════════════════════════════════════ */

    @Transactional(readOnly = true)
    public List<BuyerOrderResponse> pendingOrders() {
        return repo.findByStatusInOrderByCreatedAtAsc(
                List.of(OrderStatus.PENDING_APPROVAL, OrderStatus.CONFIRMED,
                        OrderStatus.PICKING, OrderStatus.PACKING, OrderStatus.SHIPPING)
        ).stream().map(BuyerOrderResponse::from).toList();
    }

    @Transactional
    public List<BuyerOrderResponse> allOrders(String bearerToken) {
        var orders = repo.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"
        ));
        orders.forEach(o -> backfillBuyerName(o, bearerToken));
        return orders.stream().map(BuyerOrderResponse::from).toList();
    }

    @Transactional
    public BuyerOrderResponse orderDetail(Long orderId, String bearerToken) {
        BuyerOrder order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy"));
        backfillBuyerName(order, bearerToken);
        return BuyerOrderResponse.from(order);
    }

    private void backfillBuyerName(BuyerOrder order, String bearerToken) {
        if (order.getBuyerName() == null || order.getBuyerName().isBlank()) {
            String name = authClient.getUserName(bearerToken, order.getBuyerId());
            if (name != null && !name.isBlank()) {
                order.setBuyerName(name);
                repo.save(order);
            }
        }
    }

    /* ═══════════════════════════════════════════════════════════════════════
       BƯỚC 1 — PENDING_APPROVAL: Duyệt / Từ chối / Chỉnh sửa SL
       ═══════════════════════════════════════════════════════════════════════ */

    /** Duyệt đơn: kiểm tra tồn kho → reserve → CONFIRMED */
    @Transactional
    public BuyerOrderResponse approve(Long pharmacistId, Long orderId, String bearerToken) {
        BuyerOrder order = getEditable(orderId, OrderStatus.PENDING_APPROVAL);

        // ── Release any stale reservation from a previous failed approve ──
        try { inventoryClient.release(bearerToken, "BUYER_ORDER", String.valueOf(orderId)); }
        catch (Exception ignored) { /* no previous reservation — fine */ }

        // Check inventory
        List<Map<String, Object>> insufficientItems = new ArrayList<>();
        for (BuyerOrderItem item : order.getItems()) {
            int requiredBaseQty = toBaseQty(item);
            try {
                var summaries = inventoryClient.getStockSummaries(bearerToken, item.getMedicineId());
                long available = 0;
                for (var s : summaries) {
                    if (Objects.equals(toLong(s.get("medicineId")), item.getMedicineId())) {
                        available = toLong(s.get("availableQty"));
                        break;
                    }
                }
                if (available < requiredBaseQty) {
                    insufficientItems.add(Map.of(
                            "medicineId", item.getMedicineId(),
                            "medicineName", item.getMedicineName() != null ? item.getMedicineName() : "ID:" + item.getMedicineId(),
                            "requiredQty", item.getQty(),
                            "unitLabel", item.getUnitLabel() != null ? item.getUnitLabel() : "",
                            "requiredBaseQty", requiredBaseQty,
                            "availableQty", available
                    ));
                }
            } catch (Exception e) {
                insufficientItems.add(Map.of(
                        "medicineId", item.getMedicineId(),
                        "medicineName", item.getMedicineName() != null ? item.getMedicineName() : "ID:" + item.getMedicineId(),
                        "requiredQty", item.getQty(),
                        "unitLabel", item.getUnitLabel() != null ? item.getUnitLabel() : "",
                        "requiredBaseQty", requiredBaseQty,
                        "availableQty", 0L,
                        "error", "Không kiểm tra được tồn kho: " + e.getMessage()
                ));
            }
        }

        if (!insufficientItems.isEmpty()) {
            StringBuilder msg = new StringBuilder("INSUFFICIENT_STOCK|Không đủ tồn kho để duyệt đơn. ");
            for (var item : insufficientItems) {
                msg.append(String.format("%s (cần %s, có %s); ",
                        item.get("medicineName"), item.get("requiredQty"), item.get("availableQty")));
            }
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, msg.toString());
        }

        // Reserve inventory (fresh, after release)
        try {
            List<InventoryReserveRequest.Item> reserveItems = order.getItems().stream()
                    .map(it -> new InventoryReserveRequest.Item(it.getMedicineId(), toBaseQty(it)))
                    .toList();
            inventoryClient.reserve(bearerToken,
                    new InventoryReserveRequest("BUYER_ORDER", String.valueOf(orderId), "FEFO", reserveItems));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "RESERVE_FAILED|Không thể giữ chỗ tồn kho: " + e.getMessage());
        }

// Set người xử lý
        order.setProcessedBy(pharmacistId);

        // Duyệt đơn — chỉ chuyển sang CONFIRMED.
        // PaymentStatus đã được set khi BUYER tạo đơn (COD=UNPAID, BANK_TRANSFER=PAID).
        // PHARMACIST không liên quan đến thanh toán.
        order.setStatus(OrderStatus.CONFIRMED);

        BuyerOrderResponse resp = BuyerOrderResponse.from(repo.save(order));
        notificationClient.notifyOrderStatus(order.getBuyerId(), orderId,
                "Đơn hàng #" + orderId + " đã được duyệt",
                "Đơn hàng của bạn đã được duyệt và sẽ sớm được xử lý.");
        return resp;
    }

    /** Từ chối đơn ở bước 1 */
    @Transactional
    public BuyerOrderResponse reject(Long pharmacistId, Long orderId, String reason, String bearerToken) {
        BuyerOrder order = getEditable(orderId, OrderStatus.PENDING_APPROVAL);
        order.setStatus(OrderStatus.REJECTED);
        order.setProcessedBy(pharmacistId);
        order.setRejectionReason(reason);
        try { inventoryClient.release(bearerToken, "BUYER_ORDER", String.valueOf(orderId)); }
        catch (Exception e) { /* no reservation */ }

        // PAID → hoàn tiền vào ví Buyer
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            PaymentMethod pm = order.getPaymentMethod();
            if (pm == PaymentMethod.BANK_TRANSFER || pm == PaymentMethod.STRIPE
                    || pm == PaymentMethod.WALLET || pm == PaymentMethod.ONLINE) {
                walletService.refund(order.getBuyerId(), order.getTotal(), orderId,
                        "Hoàn tiền đơn #" + orderId + " (bị từ chối)");
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            }
        }

        BuyerOrderResponse resp = BuyerOrderResponse.from(repo.save(order));
        notificationClient.notifyOrderStatus(order.getBuyerId(), orderId,
                "Đơn hàng #" + orderId + " bị từ chối",
                (order.getPaymentStatus() == PaymentStatus.REFUNDED
                    ? "Đã hoàn " + order.getTotal() + "đ vào ví. "
                    : "")
                + "Lý do: " + (reason != null ? reason : "Không rõ"));
        return resp;
    }

    /** Chỉnh sửa SL/giá ở bước 1 (trước khi duyệt) */
    @Transactional
    public BuyerOrderResponse adjust(Long pharmacistId, Long orderId, AdjustOrderRequest req) {
        BuyerOrder order = getEditable(orderId, OrderStatus.PENDING_APPROVAL);
        if (req.items() != null) {
            for (var adj : req.items()) {
                BuyerOrderItem item = order.getItems().stream()
                        .filter(it -> it.getId().equals(adj.itemId())).findFirst()
                        .orElseThrow(() -> new RuntimeException("Item không tìm thấy: " + adj.itemId()));
                if (adj.newQty() != null && adj.newQty() > 0) {
                    item.setQty(adj.newQty());
                    item.setOriginalQty(adj.newQty());
                }
                if (adj.newPrice() != null) item.setUnitPrice(adj.newPrice());
                item.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQty())));
            }
        }
        recalcTotals(order);
        String existing = order.getNotes() != null ? order.getNotes() : "";
        if (req.adjustNote() != null && !req.adjustNote().isBlank()) {
            order.setNotes(existing + "\n[ADJUST] " + req.adjustNote());
        }
        order.setProcessedBy(pharmacistId);
        return BuyerOrderResponse.from(repo.save(order));
    }

    /* ═══════════════════════════════════════════════════════════════════════
       BƯỚC 2→3 — Chuyển trạng thái chung (CONFIRMED→PICKING, etc.)
       ═══════════════════════════════════════════════════════════════════════ */

    @Transactional
    public BuyerOrderResponse updateStatus(Long pharmacistId, Long orderId, OrderStatus newStatus, String bearerToken) {
        BuyerOrder order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy"));
        Set<OrderStatus> allowed = VALID_TRANSITIONS.get(order.getStatus());
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new RuntimeException("Không thể chuyển từ " + order.getStatus() + " sang " + newStatus);
        }

        // PICKING → PACKING: commit inventory (trừ kho vĩnh viễn)
        if (order.getStatus() == OrderStatus.PICKING && newStatus == OrderStatus.PACKING) {
            try {
                inventoryClient.commit(bearerToken, "BUYER_ORDER", String.valueOf(orderId));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "COMMIT_FAILED|Không thể trừ tồn kho: " + e.getMessage());
            }
        }

        order.setStatus(newStatus);
        order.setProcessedBy(pharmacistId);
        return BuyerOrderResponse.from(repo.save(order));
    }

    /* ═══════════════════════════════════════════════════════════════════════
       BƯỚC 3 — PICKING: Partial Fulfillment (báo thiếu hàng)
       ═══════════════════════════════════════════════════════════════════════ */

    @Transactional
    public BuyerOrderResponse partialFulfillment(Long pharmacistId, Long orderId,
                                                  PartialFulfillmentRequest req, String bearerToken) {
        BuyerOrder order = getEditable(orderId, OrderStatus.PICKING);
        List<InventoryClient.AdjustReservationItem> inventoryAdjustments = new ArrayList<>();

        if (req.items() != null) {
            for (var upd : req.items()) {
                BuyerOrderItem item = order.getItems().stream()
                        .filter(it -> it.getId().equals(upd.itemId())).findFirst()
                        .orElseThrow(() -> new RuntimeException("Item không tìm thấy: " + upd.itemId()));

                // Mark fulfilled
                if (upd.fulfilled() != null) {
                    item.setFulfilled(upd.fulfilled());
                }

                // Reduce qty (partial fulfillment)
                if (upd.actualQty() != null && upd.actualQty() < item.getQty()) {
                    int oldQty = item.getQty();
                    int delta = oldQty - upd.actualQty();

                    if (upd.actualQty() <= 0) {
                        // Remove item entirely
                        inventoryAdjustments.add(
                                new InventoryClient.AdjustReservationItem(item.getMedicineId(), oldQty));
                        order.getItems().remove(item);
                    } else {
                        // Reduce qty
                        item.setQty(upd.actualQty());
                        item.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(upd.actualQty())));
                        item.setFulfilled(true);
                        inventoryAdjustments.add(
                                new InventoryClient.AdjustReservationItem(item.getMedicineId(), delta));
                    }
                }
            }
        }

        recalcTotals(order);
        order.setProcessedBy(pharmacistId);

        // Release excess reservation in inventory
        if (!inventoryAdjustments.isEmpty()) {
            try {
                inventoryClient.adjustReservation(bearerToken, "BUYER_ORDER",
                        String.valueOf(orderId), inventoryAdjustments);
            } catch (Exception e) {
                // Log but don't block — inventory will reconcile
                String existing = order.getNotes() != null ? order.getNotes() : "";
                order.setNotes(existing + "\n[WARN] adjustReservation failed: " + e.getMessage());
            }
        }

        return BuyerOrderResponse.from(repo.save(order));
    }

    /* ═══════════════════════════════════════════════════════════════════════
       BƯỚC 4 → 5 — PACKING → SHIPPING: Giao cho Shipper
       ═══════════════════════════════════════════════════════════════════════ */

    @Transactional
    public BuyerOrderResponse shipOrder(Long pharmacistId, Long orderId, ShipOrderRequest req) {
        BuyerOrder order = getEditable(orderId, OrderStatus.PACKING);
        order.setStatus(OrderStatus.SHIPPING);
        order.setCarrier(req.carrier());
        order.setTrackingCode(req.trackingCode());
        order.setShipperName(req.shipperName());
        order.setShipperPhone(req.shipperPhone());
        order.setShippedAt(LocalDateTime.now());
        if (req.notes() != null && !req.notes().isBlank()) {
            String existing = order.getNotes() != null ? order.getNotes() : "";
            order.setNotes(existing + "\n[SHIPPING] " + req.notes());
        }
        order.setProcessedBy(pharmacistId);
        BuyerOrderResponse resp = BuyerOrderResponse.from(repo.save(order));
        notificationClient.notifyOrderStatus(order.getBuyerId(), orderId,
                "Đơn hàng #" + orderId + " đang giao",
                "Đơn hàng đã được giao cho " + (req.carrier() != null ? req.carrier() : "shipper") + ".");
        return resp;
    }

    /* ═══════════════════════════════════════════════════════════════════════
       BƯỚC 5 → 6 — SHIPPING → DELIVERED/REJECTED: Kết quả giao hàng
       ═══════════════════════════════════════════════════════════════════════ */

    @Transactional
    public BuyerOrderResponse deliveryResult(Long pharmacistId, Long orderId,
                                              DeliveryResultRequest req, String bearerToken) {
        BuyerOrder order = getEditable(orderId, OrderStatus.SHIPPING);
        order.setProcessedBy(pharmacistId);

        if (req.success()) {
            // Giao thành công
            order.setStatus(OrderStatus.DELIVERED);
            order.setDeliveredAt(LocalDateTime.now());
            // COD: auto PAID
            if (order.getPaymentMethod() == PaymentMethod.COD) {
                order.setPaymentStatus(PaymentStatus.PAID);
            }
        } else {
            // Giao thất bại
            order.setStatus(OrderStatus.REJECTED);
            order.setRejectionReason(req.failureReason() != null ? req.failureReason() : "DELIVERY_FAILED");
            // Hoàn kho
            try { inventoryClient.returnStock(bearerToken, "BUYER_ORDER", String.valueOf(orderId)); }
            catch (Exception e) { /* log but don't block */ }
            // Online: refund
            if (order.getPaymentMethod() == PaymentMethod.ONLINE
                    && order.getPaymentStatus() == PaymentStatus.PAID) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            }
        }

        BuyerOrderResponse resp = BuyerOrderResponse.from(repo.save(order));
        if (req.success()) {
            notificationClient.notifyOrderStatus(order.getBuyerId(), orderId,
                    "Đơn hàng #" + orderId + " đã giao thành công",
                    "Đơn hàng đã được giao. Vui lòng xác nhận nhận hàng.");
        } else {
            notificationClient.notifyOrderStatus(order.getBuyerId(), orderId,
                    "Đơn hàng #" + orderId + " giao thất bại",
                    "Lý do: " + (req.failureReason() != null ? req.failureReason() : "Không rõ"));
        }
        return resp;
    }

    /* ═══════════════════════════════════════════════════════════════════════
       BƯỚC 6 — DELIVERED → RETURNED: Hoàn trả
       ═══════════════════════════════════════════════════════════════════════ */

    @Transactional
    public BuyerOrderResponse returnOrder(Long pharmacistId, Long orderId, String reason, String bearerToken) {
        BuyerOrder order = getEditable(orderId, OrderStatus.DELIVERED);
        order.setStatus(OrderStatus.RETURNED);
        order.setReturnedAt(LocalDateTime.now());
        order.setReturnReason(reason);
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order.setProcessedBy(pharmacistId);

        // Hoàn kho
        try { inventoryClient.returnStock(bearerToken, "BUYER_ORDER", String.valueOf(orderId)); }
        catch (Exception e) { /* log — inventory reconcile later */ }

        return BuyerOrderResponse.from(repo.save(order));
    }

    /* ═══════════════════════════════════════════════════════════════════════
       PHARMACIST CANCEL — hủy ở nhiều trạng thái
       ═══════════════════════════════════════════════════════════════════════ */

    @Transactional
    public BuyerOrderResponse cancelByPharmacist(Long pharmacistId, Long orderId, String reason, String bearerToken) {
        BuyerOrder order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy"));

        Set<OrderStatus> cancellable = Set.of(
                OrderStatus.PENDING_APPROVAL, OrderStatus.CONFIRMED,
                OrderStatus.PICKING, OrderStatus.PACKING
        );
        if (!cancellable.contains(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Không thể hủy đơn ở trạng thái: " + order.getStatus());
        }

        OrderStatus prevStatus = order.getStatus();
        order.setStatus(OrderStatus.REJECTED);
        order.setRejectionReason(reason != null && !reason.isBlank() ? reason : "PHARMACIST_CANCELLED");
        order.setProcessedBy(pharmacistId);

        // Handle inventory based on previous status
        String refId = String.valueOf(orderId);
        try {
            if (prevStatus == OrderStatus.CONFIRMED) {
                inventoryClient.release(bearerToken, "BUYER_ORDER", refId);
            } else if (prevStatus == OrderStatus.PICKING || prevStatus == OrderStatus.PACKING) {
                inventoryClient.returnStock(bearerToken, "BUYER_ORDER", refId);
            }
        } catch (Exception e) { /* log but don't block */ }

        // PAID → hoàn tiền vào ví Buyer
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            PaymentMethod pm = order.getPaymentMethod();
            if (pm == PaymentMethod.BANK_TRANSFER || pm == PaymentMethod.STRIPE
                    || pm == PaymentMethod.WALLET || pm == PaymentMethod.ONLINE) {
                walletService.refund(order.getBuyerId(), order.getTotal(), orderId,
                        "Hoàn tiền đơn #" + orderId + " (bị hủy)");
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            }
        }

        BuyerOrderResponse resp = BuyerOrderResponse.from(repo.save(order));
        notificationClient.notifyOrderStatus(order.getBuyerId(), orderId,
                "Đơn hàng #" + orderId + " đã bị hủy",
                (order.getPaymentStatus() == PaymentStatus.REFUNDED
                    ? "Đã hoàn " + order.getTotal() + "đ vào ví. "
                    : "")
                + "Lý do: " + (reason != null && !reason.isBlank() ? reason : "PHARMACIST_CANCELLED"));
        return resp;
    }

    /* ═══════════════════════════════════════════════════════════════════════
       PHARMACIST — xác nhận thanh toán COD
       ═══════════════════════════════════════════════════════════════════════ */

    @Transactional
    public BuyerOrderResponse confirmPayment(Long orderId) {
        BuyerOrder order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy"));
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Chỉ xác nhận thanh toán khi đơn đã giao");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Đơn hàng đã được thanh toán");
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        return BuyerOrderResponse.from(repo.save(order));
    }

    /* ═══════════════════════════════════════════════════════════════════════
       BUYER — xác nhận đã nhận hàng
       ═══════════════════════════════════════════════════════════════════════ */

    @Transactional
    public BuyerOrderResponse buyerConfirmReceived(Long buyerId, Long orderId) {
        BuyerOrder order = repo.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy"));
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Đơn hàng chưa ở trạng thái đã giao");
        }
        order.setBuyerConfirmed(true);
        return BuyerOrderResponse.from(repo.save(order));
    }

    /* ═══════════════════════════════════════════════════════════════════════
       PAYMENT WEBHOOK
       ═══════════════════════════════════════════════════════════════════════ */

    /**
     * Được gọi từ PaymentController khi Stripe/cổng thanh toán callback thành công.
     * Idempotent: an toàn nếu gọi nhiều lần.
     */
    @Transactional
    public BuyerOrderResponse handlePaymentWebhook(Long orderId, String transactionId) {
        BuyerOrder order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy: " + orderId));

        // Idempotent — bỏ qua nếu đã thanh toán
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return BuyerOrderResponse.from(order);
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Đơn không ở trạng thái CONFIRMED, không thể xác nhận thanh toán");
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaymentTransactionId(transactionId);
        order.setStatus(OrderStatus.PICKING);
        return BuyerOrderResponse.from(repo.save(order));
    }

    /**
     * SePay webhook: xác nhận thanh toán chuyển khoản ngân hàng.
     * Khác với handlePaymentWebhook() — cho phép ở trạng thái PENDING_APPROVAL.
     * Khi buyer chọn BANK_TRANSFER, đơn được tạo UNPAID + PENDING_APPROVAL.
     * Khi SePay gọi webhook → chỉ set paymentStatus=PAID, KHÔNG đổi orderStatus.
     * Pharmacist vẫn cần duyệt đơn bình thường.
     */
    @Transactional
    public BuyerOrderResponse handleSePayPayment(Long orderId, String transactionId) {
        BuyerOrder order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy: " + orderId));

        // Idempotent
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return BuyerOrderResponse.from(order);
        }

        // Cho phép ở nhiều trạng thái (PENDING_APPROVAL, CONFIRMED, etc.)
        // Chỉ set PAID, không tự động đổi orderStatus
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaymentTransactionId(transactionId);
        return BuyerOrderResponse.from(repo.save(order));
    }

    /* ═══════════════════════════════════════════════════════════════════════
       HELPERS
       ═══════════════════════════════════════════════════════════════════════ */

    private BuyerOrder getEditable(Long orderId, OrderStatus expectedStatus) {
        BuyerOrder order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy"));
        if (order.getStatus() != expectedStatus) {
            throw new RuntimeException("Không thể xử lý đơn ở trạng thái: " + order.getStatus());
        }
        return order;
    }

    private void recalcTotals(BuyerOrder order) {
        BigDecimal subtotal = order.getItems().stream()
                .map(BuyerOrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);
        order.setDiscount(order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO);
        order.setTotal(subtotal.subtract(order.getDiscount()));
    }

    private static long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        if (o instanceof String s) try { return Long.parseLong(s); } catch (Exception e) { /* ignore */ }
        return 0;
    }

    /** Parse paymentStatus string từ FE → PaymentStatus enum. Default = UNPAID. */
    private static PaymentStatus parsePaymentStatus(String s) {
        if ("PAID".equalsIgnoreCase(s)) return PaymentStatus.PAID;
        return PaymentStatus.UNPAID;
    }

    /* ═══════════════════════════════════════════════════════════════════════
       QR CODE GENERATOR (VIETQR)
       ═══════════════════════════════════════════════════════════════════════ */
    private String buildVietQrUrl(BuyerOrder order) {
        String bankId = "BIDV";
        String accountNo = "96247A5YJ2";
        String accountName = "PHAM GIA HIEP";
        
        long amount = order.getTotal().longValue();
        
        // Nội dung chuyển khoản chứa Mã đơn hàng để SePay webhook khớp
        String addInfo = "DH" + order.getId(); 

        // VietQR URL builder
        return String.format(
            "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
            bankId, 
            accountNo, 
            amount, 
            addInfo.replace(" ", "%20"), 
            accountName.replace(" ", "%20")
        );
    }
}
