package com.pharmacy.sales_service.controller;

import com.pharmacy.sales_service.dto.BuyerOrderResponse;
import com.pharmacy.sales_service.dto.SePayWebhookPayload;
import com.pharmacy.sales_service.entity.BuyerOrder;
import com.pharmacy.sales_service.entity.OrderStatus;
import com.pharmacy.sales_service.entity.PaymentStatus;
import com.pharmacy.sales_service.repository.BuyerOrderRepo;
import com.pharmacy.sales_service.service.BuyerOrderService;
import com.pharmacy.sales_service.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final BuyerOrderService orderService;
    private final StripeService stripeService;
    private final BuyerOrderRepo repo;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${sepay.api-key}")
    private String sePayApiKey;

    /* ═══════════════════════════════════════════════════════════════════════
       SEPAY WEBHOOK — Tự động xác nhận chuyển khoản ngân hàng
       ═══════════════════════════════════════════════════════════════════════ */

    /**
     * SePay gọi endpoint này khi có giao dịch tiền vào tài khoản.
     * KHÔNG cần JWT — xác thực bằng API Key trong header Authorization.
     *
     * Flow:
     * 1. Validate API Key
     * 2. Chỉ xử lý transferType = "in" (tiền vào)
     * 3. Parse nội dung chuyển khoản → tìm mã đơn hàng (DH{id})
     * 4. Kiểm tra số tiền >= tổng đơn
     * 5. Cập nhật paymentStatus = PAID
     */
    @PostMapping("/webhook/sepay")
    public ResponseEntity<?> sePayWebhook(
            @RequestBody SePayWebhookPayload payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // 1. Validate API Key (optional — SePay có thể gửi không kèm API key)
        if (authHeader != null && !authHeader.isBlank()) {
            if (!authHeader.equals("Apikey " + sePayApiKey)) {
                log.warn("[SePay] Invalid API key from webhook call");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid API key"));
            }
            log.info("[SePay] API key validated OK");
        } else {
            log.info("[SePay] No API key provided (Không xác thực mode)");
        }

        // 2. Chỉ xử lý tiền VÀO
        if (!"in".equalsIgnoreCase(payload.getTransferType())) {
            log.info("[SePay] Skipping non-incoming transaction: {}", payload.getTransferType());
            return ResponseEntity.ok(Map.of("success", true, "message", "Skipped: not incoming"));
        }

        log.info("[SePay] Incoming payment: amount={}, content='{}', code='{}'",
                payload.getTransferAmount(), payload.getContent(), payload.getCode());

        // 3. Parse mã đơn hàng từ nội dung chuyển khoản
        Long orderId = extractOrderId(payload.getContent());
        if (orderId == null && payload.getCode() != null) {
            orderId = extractOrderId(payload.getCode());
        }

        if (orderId == null) {
            log.warn("[SePay] Could not extract orderId from content: '{}'", payload.getContent());
            return ResponseEntity.ok(Map.of("success", true, "message", "No orderId found in content"));
        }

        // 4. Tìm đơn hàng
        var orderOpt = repo.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.warn("[SePay] Order not found: {}", orderId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Order not found: " + orderId));
        }

        BuyerOrder order = orderOpt.get();

        // Idempotent: đã thanh toán rồi thì bỏ qua
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("[SePay] Order {} already PAID, skipping", orderId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Already paid"));
        }

        // 5. Kiểm tra số tiền (cho phép >= tổng đơn)
        long requiredAmount = order.getTotal() != null ? order.getTotal().longValue() : 0;
        if (payload.getTransferAmount() < requiredAmount) {
            log.warn("[SePay] Insufficient amount for order {}: got {}, need {}",
                    orderId, payload.getTransferAmount(), requiredAmount);
            return ResponseEntity.ok(Map.of("success", true,
                    "message", "Insufficient amount: got " + payload.getTransferAmount() + ", need " + requiredAmount));
        }

        // 6. Cập nhật thanh toán
        try {
            String transactionId = "SEPAY-" + payload.getId();
            orderService.handleSePayPayment(orderId, transactionId);
            log.info("[SePay] ✅ Order {} payment confirmed. TransactionId: {}", orderId, transactionId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment confirmed for order " + orderId));
        } catch (Exception e) {
            log.error("[SePay] Error confirming payment for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Simulate SePay webhook — CHỈ dùng cho demo/thesis.
     * FE gọi endpoint này để giả lập thanh toán thành công.
     */
    @PostMapping("/webhook/sepay/simulate/{orderId}")
    public ResponseEntity<?> simulateSePayWebhook(@PathVariable Long orderId) {
        var orderOpt = repo.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Order not found"));
        }

        BuyerOrder order = orderOpt.get();
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Already paid"));
        }

        try {
            String transactionId = "SIMULATE-" + System.currentTimeMillis();
            orderService.handleSePayPayment(orderId, transactionId);
            log.info("[SePay-Simulate] ✅ Order {} payment simulated", orderId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment simulated for order " + orderId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Trích xuất orderId từ nội dung chuyển khoản.
     * Tìm pattern "DH" + số (vd: "DH123", "DH 123", "dh123")
     */
    private Long extractOrderId(String content) {
        if (content == null || content.isBlank()) return null;
        // Pattern: DH followed by digits, case-insensitive, allowing optional space
        Pattern pattern = Pattern.compile("DH\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /* ═══════════════════════════════════════════════════════════════════════
       PAYMENT STATUS POLLING — FE poll để kiểm tra thanh toán
       ═══════════════════════════════════════════════════════════════════════ */

    /**
     * FE poll endpoint này mỗi 5 giây để check trạng thái thanh toán.
     * BUYER chỉ xem được đơn của mình (owner-check).
     */
    @GetMapping("/status/{orderId}")
    public Map<String, Object> getPaymentStatus(@PathVariable Long orderId, Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        BuyerOrder order = repo.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn"));
        return Map.of(
                "orderId", order.getId(),
                "paymentStatus", order.getPaymentStatus().name(),
                "orderStatus", order.getStatus().name(),
                "total", order.getTotal() != null ? order.getTotal() : 0
        );
    }

    /* ═══════════════════════════════════════════════════════════════════════
       STRIPE — Giữ nguyên logic cũ
       ═══════════════════════════════════════════════════════════════════════ */

    /**
     * Tạo Stripe PaymentIntent — FE gọi để lấy clientSecret.
     * Tái dùng intent đã tạo nếu order đã có stripePaymentIntentId.
     */
    @PostMapping("/stripe/create-intent/{orderId}")
    public Map<String, String> createIntent(@PathVariable Long orderId,
                                             Authentication auth) throws Exception {
        Long buyerId = (Long) auth.getPrincipal();
        BuyerOrder order = repo.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn"));

        if (order.getStatus() != OrderStatus.CONFIRMED
                || order.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Đơn không ở trạng thái chờ thanh toán");
        }

        // Tái dùng intent đã tạo (idempotent)
        if (order.getStripePaymentIntentId() != null) {
            PaymentIntent existing = stripeService.retrieveIntent(order.getStripePaymentIntentId());
            return Map.of("clientSecret", existing.getClientSecret());
        }

        // ⚠️ VND là zero-decimal — truyền thẳng số nguyên, KHÔNG nhân 100
        long amountVnd = order.getTotal().longValue();
        PaymentIntent pi = stripeService.createIntent(amountVnd, orderId);

        order.setStripePaymentIntentId(pi.getId());
        repo.save(order);

        return Map.of("clientSecret", pi.getClientSecret());
    }

    /**
     * Tạo Stripe PaymentIntent cho GIỎ HÀNG (trước khi tạo đơn).
     * FE gửi amountVnd, BE tạo PaymentIntent và trả về clientSecret.
     * ⚠️ VND là zero-decimal — truyền thẳng số nguyên.
     */
@PostMapping("/stripe/create-cart-intent")
public Map<String, String> createCartIntent(@RequestBody Map<String, Long> body,
                                            Authentication auth) throws Exception {
    Long buyerId = (Long) auth.getPrincipal();

    Long amountVnd = body.get("amountVnd");
    if (amountVnd == null || amountVnd <= 0) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amountVnd không hợp lệ");
    }

    PaymentIntent pi = stripeService.createIntent(amountVnd, -1L);

    return Map.of(
            "clientSecret", pi.getClientSecret(),
            "paymentIntentId", pi.getId()
    );
}

    /**
     * Stripe Webhook — KHÔNG cần JWT.
     * Xác thực bằng chữ ký Stripe-Signature.
     */
    @PostMapping("/webhook/stripe")
    public ResponseEntity<?> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Stripe signature");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                PaymentIntent pi = (PaymentIntent) obj;
                String orderIdStr = pi.getMetadata().get("orderId");
                if (orderIdStr != null) {
                    try {
                        orderService.handlePaymentWebhook(Long.parseLong(orderIdStr), pi.getId());
                    } catch (Exception ignored) { /* log in production */ }
                }
            });
        }

        return ResponseEntity.ok(Map.of("received", true));
    }
}
