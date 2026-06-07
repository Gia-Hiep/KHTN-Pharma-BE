package com.pharmacy.customer_service.repository;

import com.pharmacy.customer_service.entity.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoyaltyTransactionRepo extends JpaRepository<LoyaltyTransaction, Long> {

    List<LoyaltyTransaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT COALESCE(SUM(t.points), 0) FROM LoyaltyTransaction t WHERE t.customerId = :customerId")
    Integer sumPoints(@Param("customerId") Long customerId);

    List<LoyaltyTransaction> findByCustomerIdAndTypeOrderByCreatedAtDesc(Long customerId, String type);
}
