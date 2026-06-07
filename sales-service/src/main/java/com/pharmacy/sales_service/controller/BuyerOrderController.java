package com.pharmacy.sales_service.controller;

import com.pharmacy.sales_service.dto.BuyerOrderResponse;
import com.pharmacy.sales_service.dto.CreateOrderRequest;
import com.pharmacy.sales_service.service.BuyerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints cho BUYER đặt hàng online.
 * Owner-check được thực thi ở service layer.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class BuyerOrderController {

    private final BuyerOrderService service;

    /** Tạo đơn hàng mới */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BuyerOrderResponse create(@RequestBody CreateOrderRequest req,
                                      @RequestHeader("Authorization") String authHeader,
                                      Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        String token = authHeader.replace("Bearer ", "");
        return service.create(buyerId, req, token);
    }

    /** Danh sách đơn của BUYER đang đăng nhập */
    @GetMapping
    public List<BuyerOrderResponse> myOrders(Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        return service.myOrders(buyerId);
    }

    /** Chi tiết đơn — owner-check: chỉ BUYER sở hữu đơn mới xem được */
    @GetMapping("/{id}")
    public BuyerOrderResponse myDetail(@PathVariable Long id, Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        return service.myOrderDetail(buyerId, id);
    }

    /** BUYER hủy đơn — chỉ khi PENDING_APPROVAL */
    @PostMapping("/{id}/cancel")
    public BuyerOrderResponse cancel(@PathVariable Long id,
                                      @RequestHeader("Authorization") String authHeader,
                                      Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        String token = authHeader.replace("Bearer ", "");
        return service.cancelOrder(buyerId, id, token);
    }

    /** BUYER xác nhận đã nhận hàng */
    @PostMapping("/{id}/confirm-received")
    public BuyerOrderResponse confirmReceived(@PathVariable Long id, Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        return service.buyerConfirmReceived(buyerId, id);
    }
}
