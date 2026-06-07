package com.pharmacy.customer_service.controller;

import com.pharmacy.customer_service.dto.ApplyCouponRequest;
import com.pharmacy.customer_service.dto.ApplyCouponResponse;
import com.pharmacy.customer_service.dto.CreateCouponRequest;
import com.pharmacy.customer_service.entity.Coupon;
import com.pharmacy.customer_service.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/customers/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService service;

    /**
     * Danh sách coupons đang active
     */
    @GetMapping
    public List<Coupon> list() {
        return service.listActiveCoupons();
    }

    /**
     * Chi tiết coupon
     */
    @GetMapping("/{id}")
    public Coupon get(@PathVariable Long id) {
        return service.getCoupon(id);
    }

    /**
     * Tìm coupon theo code
     */
    @GetMapping("/code/{code}")
    public Coupon getByCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    /**
     * Tạo coupon mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon create(@Valid @RequestBody CreateCouponRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return service.createCoupon(req, userId);
    }

    /**
     * Validate coupon mà không apply
     */
    @GetMapping("/validate")
    public ApplyCouponResponse validate(
            @RequestParam String code,
            @RequestParam Long customerId,
            @RequestParam BigDecimal orderAmount
    ) {
        return service.validateCoupon(code, customerId, orderAmount);
    }

    /**
     * Apply coupon vào đơn hàng
     */
    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ApplyCouponResponse apply(@Valid @RequestBody ApplyCouponRequest req) {
        return service.applyCoupon(req);
    }

    /**
     * Deactivate coupon
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable Long id) {
        service.deactivateCoupon(id);
    }
}
