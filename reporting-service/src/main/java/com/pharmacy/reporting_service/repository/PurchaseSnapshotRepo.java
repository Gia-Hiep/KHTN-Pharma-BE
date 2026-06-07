package com.pharmacy.reporting_service.repository;

import com.pharmacy.reporting_service.entity.PurchaseSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseSnapshotRepo extends JpaRepository<PurchaseSnapshot, Long> {

    Optional<PurchaseSnapshot> findByReportDate(LocalDate date);

    List<PurchaseSnapshot> findByReportDateBetweenOrderByReportDateAsc(LocalDate from, LocalDate to);
}
