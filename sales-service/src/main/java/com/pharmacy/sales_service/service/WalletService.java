package com.pharmacy.sales_service.service;

import com.pharmacy.sales_service.entity.Wallet;
import com.pharmacy.sales_service.entity.WalletTransaction;
import com.pharmacy.sales_service.entity.WalletTransaction.TransactionType;
import com.pharmacy.sales_service.repository.WalletRepo;
import com.pharmacy.sales_service.repository.WalletTransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepo walletRepo;
    private final WalletTransactionRepo txRepo;

    /** Lấy hoặc tạo ví cho buyer */
    @Transactional
    public Wallet getOrCreate(Long buyerId) {
        return walletRepo.findByBuyerId(buyerId)
                .orElseGet(() -> walletRepo.save(
                        Wallet.builder().buyerId(buyerId).balance(BigDecimal.ZERO).build()
                ));
    }

    /** Lấy số dư */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long buyerId) {
        return walletRepo.findByBuyerId(buyerId)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    /** Hoàn tiền vào ví (khi hủy đơn PAID) */
    @Transactional
    public WalletTransaction refund(Long buyerId, BigDecimal amount, Long orderId, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số tiền hoàn phải > 0");
        }

        Wallet wallet = getOrCreate(buyerId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepo.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .buyerId(buyerId)
                .type(TransactionType.REFUND)
                .amount(amount)
                .balanceAfter(wallet.getBalance())
                .orderId(orderId)
                .description(description != null ? description : "Hoàn tiền đơn hàng #" + orderId)
                .build();
        txRepo.save(tx);

        log.info("[Wallet] Refund {}đ to buyer {} (order #{}). New balance: {}",
                amount, buyerId, orderId, wallet.getBalance());
        return tx;
    }

    /** Trừ ví khi thanh toán bằng WALLET */
    @Transactional
    public WalletTransaction pay(Long buyerId, BigDecimal amount, Long orderId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số tiền thanh toán phải > 0");
        }

        Wallet wallet = getOrCreate(buyerId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Số dư ví không đủ. Hiện có: " + wallet.getBalance() + "đ, cần: " + amount + "đ");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepo.save(wallet);

        WalletTransaction tx = WalletTransaction.builder()
                .buyerId(buyerId)
                .type(TransactionType.PAYMENT)
                .amount(amount)
                .balanceAfter(wallet.getBalance())
                .orderId(orderId)
                .description("Thanh toán đơn hàng #" + orderId)
                .build();
        txRepo.save(tx);

        log.info("[Wallet] Payment {}đ from buyer {} (order #{}). New balance: {}",
                amount, buyerId, orderId, wallet.getBalance());
        return tx;
    }

    /** Lịch sử giao dịch */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTransactions(Long buyerId) {
        return txRepo.findByBuyerIdOrderByCreatedAtDesc(buyerId).stream()
                .map(tx -> Map.<String, Object>of(
                        "id", tx.getId(),
                        "type", tx.getType().name(),
                        "amount", tx.getAmount(),
                        "balanceAfter", tx.getBalanceAfter(),
                        "orderId", tx.getOrderId() != null ? tx.getOrderId() : 0,
                        "description", tx.getDescription() != null ? tx.getDescription() : "",
                        "createdAt", tx.getCreatedAt().toString()
                ))
                .toList();
    }
}
