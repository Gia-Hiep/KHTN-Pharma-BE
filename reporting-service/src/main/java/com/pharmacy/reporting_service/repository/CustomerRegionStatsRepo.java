package com.pharmacy.reporting_service.repository;

import com.pharmacy.reporting_service.entity.CustomerRegionStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CustomerRegionStatsRepo extends JpaRepository<CustomerRegionStats, Long> {

    List<CustomerRegionStats> findByReportMonthOrderByTotalRevenueDesc(LocalDate reportMonth);

    List<CustomerRegionStats> findByReportMonthOrderByCustomerCountDesc(LocalDate reportMonth);
}
