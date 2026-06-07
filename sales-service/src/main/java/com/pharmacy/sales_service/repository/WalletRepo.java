package com.pharmacy.sales_service.repository;

import com.pharmacy.sales_service.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepo extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByBuyerId(Long buyerId);
}
