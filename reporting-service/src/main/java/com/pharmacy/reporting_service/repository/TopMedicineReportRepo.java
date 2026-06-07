package com.pharmacy.reporting_service.repository;

import com.pharmacy.reporting_service.entity.TopMedicineReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TopMedicineReportRepo extends JpaRepository<TopMedicineReport, Long> {

    List<TopMedicineReport> findByReportMonthOrderByRankInMonthAsc(LocalDate reportMonth);

    List<TopMedicineReport> findByReportMonthOrderByTotalQuantitySoldDesc(LocalDate reportMonth);
}
