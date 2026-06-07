package com.pharmacy.reporting_service.repository;

import com.pharmacy.reporting_service.entity.SalesSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesSnapshotRepo extends JpaRepository<SalesSnapshot, Long> {

    List<SalesSnapshot> findByReportDateBetweenOrderByReportDateAsc(LocalDate from, LocalDate to);

    Optional<SalesSnapshot> findByReportDateAndOrderType(LocalDate date, String orderType);

    List<SalesSnapshot> findByReportDateAndOrderTypeIn(LocalDate date, List<String> orderTypes);

    @Query("""
            select s from SalesSnapshot s
            where s.reportDate between :from and :to
              and s.orderType = :orderType
            order by s.reportDate asc
            """)
    List<SalesSnapshot> findByDateRangeAndOrderType(LocalDate from, LocalDate to, String orderType);

    @Query("""
            select sum(s.netRevenue) from SalesSnapshot s
            where s.reportDate between :from and :to
              and s.orderType = 'ALL'
            """)
    BigDecimal sumNetRevenueBetween(LocalDate from, LocalDate to);
}
