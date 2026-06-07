package com.pharmacy.reporting_service.repository;

import com.pharmacy.reporting_service.entity.InventorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventorySnapshotRepo extends JpaRepository<InventorySnapshot, Long> {

    List<InventorySnapshot> findBySnapshotDate(LocalDate date);

    Optional<InventorySnapshot> findBySnapshotDateAndMedicineId(LocalDate date, Long medicineId);

    /** Thuốc sắp hết hàng */
    List<InventorySnapshot> findBySnapshotDateAndStockStatus(LocalDate date, String stockStatus);

    /** Lấy snapshot mới nhất của từng sản phẩm */
    @Query("""
            select s from InventorySnapshot s
            where s.snapshotDate = :date
              and s.stockStatus in ('LOW_STOCK', 'OUT_OF_STOCK')
            order by s.quantityOnHand asc
            """)
    List<InventorySnapshot> findLowStockOnDate(LocalDate date);
}
