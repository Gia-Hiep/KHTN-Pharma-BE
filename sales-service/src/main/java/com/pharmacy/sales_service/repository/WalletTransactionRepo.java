package com.pharmacy.sales_service.repository;

import com.pharmacy.sales_service.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepo extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
}
