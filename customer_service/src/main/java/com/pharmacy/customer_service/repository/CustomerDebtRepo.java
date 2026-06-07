package com.pharmacy.customer_service.repository;

import com.pharmacy.customer_service.entity.CustomerDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerDebtRepo extends JpaRepository<CustomerDebt, Long> {

    List<CustomerDebt> findByCustomerId(Long customerId);

    List<CustomerDebt> findByCustomerIdAndStatus(Long customerId, String status);

    @Query("SELECT SUM(d.amount - d.paidAmount) FROM CustomerDebt d WHERE d.customerId = :customerId AND d.status IN ('PENDING', 'PARTIAL', 'OVERDUE')")
    BigDecimal sumRemainingDebt(@Param("customerId") Long customerId);

    @Query("SELECT d FROM CustomerDebt d WHERE d.status IN ('PENDING', 'PARTIAL') AND d.dueDate < CURRENT_DATE")
    List<CustomerDebt> findOverdueDebts();
}
