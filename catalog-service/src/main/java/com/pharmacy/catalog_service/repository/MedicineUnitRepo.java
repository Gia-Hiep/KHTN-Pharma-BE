package com.pharmacy.catalog_service.repository;

import com.pharmacy.catalog_service.entity.MedicineUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineUnitRepo extends JpaRepository<MedicineUnit, Long> {
    @Query("""
        select mu from MedicineUnit mu
        where mu.medicineId = :medicineId
        order by mu.isBaseUnit desc, mu.isDefaultSaleUnit desc, mu.id asc
    """)
    List<MedicineUnit> findByMedicineIdOrdered(@Param("medicineId") Long medicineId);

    @Query("""
        select mu from MedicineUnit mu
        where mu.medicineId = :medicineId and mu.isActive = true
        order by mu.isBaseUnit desc, mu.isDefaultSaleUnit desc, mu.id asc
    """)
    List<MedicineUnit> findActiveByMedicineIdOrdered(@Param("medicineId") Long medicineId);

    Optional<MedicineUnit> findByMedicineIdAndUnitCode(Long medicineId, String unitCode);

    Optional<MedicineUnit> findByMedicineIdAndIsBaseUnitTrue(Long medicineId);
}
