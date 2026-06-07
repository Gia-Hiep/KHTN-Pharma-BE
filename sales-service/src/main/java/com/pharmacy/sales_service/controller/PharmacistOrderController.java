package com.pharmacy.sales_service.controller;

import com.pharmacy.sales_service.dto.*;
import com.pharmacy.sales_service.entity.OrderStatus;
import com.pharmacy.sales_service.service.BuyerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pharmacist/orders")
@RequiredArgsConstructor
public class PharmacistOrderController {

    private final BuyerOrderService service;

    /* ── LIST / DETAIL ── */

    @GetMapping
    public List<BuyerOrderResponse> inbox() {
        return service.pendingOrders();
    }

    @GetMapping("/all")
    public List<BuyerOrderResponse> allOrders(@RequestHeader("Authorization") String authHeader) {
        return service.allOrders(extractToken(authHeader));
    }

    @GetMapping("/{id}")
    public BuyerOrderResponse detail(@PathVariable Long id,
                                      @RequestHeader("Authorization") String authHeader) {
        return service.orderDetail(id, extractToken(authHeader));
    }

    /* ── BƯỚC 1: PENDING_APPROVAL ── */

    @PostMapping("/{id}/approve")
    public BuyerOrderResponse approve(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader,
                                       Authentication auth) {
        return service.approve(userId(auth), id, extractToken(authHeader));
    }

    @PostMapping("/{id}/reject")
    public BuyerOrderResponse reject(@PathVariable Long id,
                                      @RequestBody Map<String, String> body,
                                      @RequestHeader("Authorization") String authHeader,
                                      Authentication auth) {
        return service.reject(userId(auth), id, body.getOrDefault("reason", ""), extractToken(authHeader));
    }

    @PostMapping("/{id}/adjust")
    public BuyerOrderResponse adjust(@PathVariable Long id,
                                      @RequestBody AdjustOrderRequest req,
                                      Authentication auth) {
        return service.adjust(userId(auth), id, req);
    }

    /* ── BƯỚC 2→3: Chuyển trạng thái chung ── */

    @PostMapping("/{id}/status")
    public BuyerOrderResponse updateStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> body,
                                            @RequestHeader("Authorization") String authHeader,
                                            Authentication auth) {
        return service.updateStatus(userId(auth), id, OrderStatus.valueOf(body.get("status")), extractToken(authHeader));
    }

    /* ── BƯỚC 3: PICKING — Partial Fulfillment ── */

    @PostMapping("/{id}/partial-fulfill")
    public BuyerOrderResponse partialFulfill(@PathVariable Long id,
                                              @RequestBody PartialFulfillmentRequest req,
                                              @RequestHeader("Authorization") String authHeader,
                                              Authentication auth) {
        return service.partialFulfillment(userId(auth), id, req, extractToken(authHeader));
    }

    /* ── BƯỚC 4→5: PACKING → SHIPPING ── */

    @PostMapping("/{id}/ship")
    public BuyerOrderResponse ship(@PathVariable Long id,
                                    @RequestBody ShipOrderRequest req,
                                    Authentication auth) {
        return service.shipOrder(userId(auth), id, req);
    }

    /* ── BƯỚC 5→6: Kết quả giao hàng ── */

    @PostMapping("/{id}/delivery-result")
    public BuyerOrderResponse deliveryResult(@PathVariable Long id,
                                              @RequestBody DeliveryResultRequest req,
                                              @RequestHeader("Authorization") String authHeader,
                                              Authentication auth) {
        return service.deliveryResult(userId(auth), id, req, extractToken(authHeader));
    }

    /* ── BƯỚC 6: DELIVERED → RETURNED ── */

    @PostMapping("/{id}/return")
    public BuyerOrderResponse returnOrder(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           @RequestHeader("Authorization") String authHeader,
                                           Authentication auth) {
        return service.returnOrder(userId(auth), id, body.getOrDefault("reason", ""), extractToken(authHeader));
    }

    /* ── XÁC NHẬN THANH TOÁN COD ── */

    @PostMapping("/{id}/confirm-payment")
    public BuyerOrderResponse confirmPayment(@PathVariable Long id) {
        return service.confirmPayment(id);
    }

    /* ── HỦY ĐƠN (nhiều trạng thái) ── */

    @PostMapping("/{id}/cancel")
    public BuyerOrderResponse cancel(@PathVariable Long id,
                                      @RequestBody Map<String, String> body,
                                      @RequestHeader("Authorization") String authHeader,
                                      Authentication auth) {
        return service.cancelByPharmacist(userId(auth), id, body.getOrDefault("reason", ""), extractToken(authHeader));
    }

    /* ── Helpers ── */

    private static Long userId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

    private static String extractToken(String authHeader) {
        return authHeader.replace("Bearer ", "");
    }
}
