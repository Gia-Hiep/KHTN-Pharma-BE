package com.pharmacy.customer_service.controller;

import com.pharmacy.customer_service.dto.CreateLoyaltyTransactionRequest;
import com.pharmacy.customer_service.entity.LoyaltyTransaction;
import com.pharmacy.customer_service.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService service;

    /**
     * Lấy lịch sử giao dịch điểm của customer
     */
    @GetMapping("/customer/{customerId}")
    public List<LoyaltyTransaction> getTransactions(@PathVariable Long customerId) {
        return service.getTransactions(customerId);
    }

    /**
     * Lấy số dư điểm của customer
     */
    @GetMapping("/customer/{customerId}/balance")
    public int getBalance(@PathVariable Long customerId) {
        return service.getBalance(customerId);
    }

    /**
     * Tích điểm từ invoice
     */
    @PostMapping("/earn")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public LoyaltyTransaction earn(
            @RequestParam Long customerId,
            @RequestParam int points,
            @RequestParam String invoiceId,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        return service.earnPoints(customerId, points, invoiceId, userId);
    }

    /**
     * Đổi điểm
     */
    @PostMapping("/redeem")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public LoyaltyTransaction redeem(
            @RequestParam Long customerId,
            @RequestParam int points,
            @RequestParam String invoiceId,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        return service.redeemPoints(customerId, points, invoiceId, userId);
    }

    /**
     * Tạo giao dịch điểm thủ công (adjust, expire)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public LoyaltyTransaction create(
            @Valid @RequestBody CreateLoyaltyTransactionRequest req,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        return service.createTransaction(req, userId);
    }
}
