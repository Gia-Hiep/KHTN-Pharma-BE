package com.pharmacy.sales_service.repository;

import com.pharmacy.sales_service.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepo extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByCode(String code);

    @Query("""
            select i from Invoice i
            where (:status is null or i.status = :status)
              and (:paymentStatus is null or i.paymentStatus = :paymentStatus)
              and (:cashierId is null or i.cashierId = :cashierId)
              and (:from is null or i.createdAt >= :from)
              and (:to is null or i.createdAt <= :to)
            order by i.createdAt desc
            """)
    List<Invoice> search(String status, String paymentStatus, Long cashierId,
                         LocalDateTime from, LocalDateTime to);


    // ===== ORDER TYPE =====
    List<Invoice> findByOrderType(String orderType);

    List<Invoice> findByOrderTypeAndStatus(String orderType, String status);

    // ===== SHIPPING =====
    List<Invoice> findByRequiresShippingTrue();

    List<Invoice> findByRequiresShippingTrueAndShippingStatus(String shippingStatus);

    // ===== CUSTOMER =====
    List<Invoice> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // ===== REPORTING =====
    @Query("""
            select i from Invoice i
            where i.createdAt >= :from and i.createdAt < :to
            """)
    List<Invoice> findByDateRange(LocalDateTime from, LocalDateTime to);

    // ===== CLEANUP =====
    List<Invoice> findByStatusAndUpdatedAtBefore(String status, LocalDateTime before);
}

