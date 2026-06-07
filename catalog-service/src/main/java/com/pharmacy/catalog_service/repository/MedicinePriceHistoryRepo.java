package com.pharmacy.catalog_service.repository;

import com.pharmacy.catalog_service.entity.MedicinePriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicinePriceHistoryRepo extends JpaRepository<MedicinePriceHistory, Long> {
}
