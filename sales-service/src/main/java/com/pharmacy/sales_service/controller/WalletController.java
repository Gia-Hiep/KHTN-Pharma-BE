package com.pharmacy.sales_service.controller;

import com.pharmacy.sales_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Wallet endpoints cho BUYER — xem số dư và lịch sử giao dịch ví.
 */
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /** Xem số dư ví */
    @GetMapping("/balance")
    public Map<String, Object> getBalance(Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        return Map.of(
                "buyerId", buyerId,
                "balance", walletService.getBalance(buyerId)
        );
    }

    /** Xem số dư + lịch sử giao dịch */
    @GetMapping
    public Map<String, Object> getWallet(Authentication auth) {
        Long buyerId = (Long) auth.getPrincipal();
        return Map.of(
                "buyerId", buyerId,
                "balance", walletService.getBalance(buyerId),
                "transactions", walletService.getTransactions(buyerId)
        );
    }
}
