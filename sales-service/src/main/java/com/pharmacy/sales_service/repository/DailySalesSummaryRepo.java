package com.pharmacy.sales_service.repository;

import com.pharmacy.sales_service.entity.DailySalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySalesSummaryRepo extends JpaRepository<DailySalesSummary, Long> {

    Optional<DailySalesSummary> findByReportDate(LocalDate reportDate);

    List<DailySalesSummary> findByReportDateBetweenOrderByReportDateDesc(
            LocalDate from, LocalDate to);
}
