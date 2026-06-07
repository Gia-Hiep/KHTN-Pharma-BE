package com.pharmacy.sales_service.controller;

import com.pharmacy.sales_service.dto.AddCartItemRequest;
import com.pharmacy.sales_service.dto.CartResponse;
import com.pharmacy.sales_service.dto.UpdateCartItemRequest;
import com.pharmacy.sales_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints cho giỏ hàng — chỉ dành cho BUYER.
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** Lấy giỏ hàng của buyer đang đăng nhập */
    @GetMapping
    public CartResponse getCart(@RequestHeader("Authorization") String authHeader,
                                Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        String token = authHeader.replace("Bearer ", "");
        return cartService.getCart(buyerId, token);
    }

    /** Thêm sản phẩm vào giỏ (cộng dồn nếu trùng medicineId + saleMode + unitCode) */
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(@RequestBody AddCartItemRequest req,
                                @RequestHeader("Authorization") String authHeader,
                                Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        String token = authHeader.replace("Bearer ", "");
        return cartService.addItem(buyerId, req, token);
    }

    /** Cập nhật số lượng 1 item */
    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(@PathVariable Long itemId,
                                   @RequestBody UpdateCartItemRequest req,
                                   @RequestHeader("Authorization") String authHeader,
                                   Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        String token = authHeader.replace("Bearer ", "");
        return cartService.updateItemQty(buyerId, itemId, req, token);
    }

    /** Xóa 1 item khỏi giỏ */
    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@PathVariable Long itemId,
                                   Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        return cartService.removeItem(buyerId, itemId);
    }

    /** Xóa toàn bộ giỏ hàng */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        cartService.clearCart(buyerId);
    }
}
