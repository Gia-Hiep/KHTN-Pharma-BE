package com.pharmacy.purchase_service.repository;

import com.pharmacy.purchase_service.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepo extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByCode(String code);

    @Query("""
            select p from Purchase p
            where (:status is null or p.status = :status)
              and (:supplierId is null or p.supplierId = :supplierId)
              and (:from is null or p.createdAt >= :from)
              and (:to is null or p.createdAt <= :to)
            order by p.createdAt desc
            """)
    List<Purchase> search(String status, Long supplierId, LocalDateTime from, LocalDateTime to);

}
