package com.pharmacy.inventory_service.repository;

import com.pharmacy.inventory_service.entity.StockAlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockAlertRuleRepo extends JpaRepository<StockAlertRule, Long> {
    Optional<StockAlertRule> findByMedicineId(Long medicineId);
}
