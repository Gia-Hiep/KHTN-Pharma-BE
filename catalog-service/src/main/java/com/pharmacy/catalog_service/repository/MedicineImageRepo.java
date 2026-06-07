package com.pharmacy.catalog_service.repository;

import com.pharmacy.catalog_service.entity.MedicineImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MedicineImageRepo extends JpaRepository<MedicineImage, Long> {

    List<MedicineImage> findByMedicineIdOrderBySortOrderAsc(Long medicineId);

    Optional<MedicineImage> findByMedicineIdAndIsPrimaryTrue(Long medicineId);

    Optional<MedicineImage> findByIdAndMedicineId(Long id, Long medicineId);

    int countByMedicineId(Long medicineId);

    @Query("SELECT COALESCE(MAX(m.sortOrder), -1) FROM MedicineImage m WHERE m.medicineId = :medicineId")
    int findMaxSortOrder(Long medicineId);

    void deleteByMedicineId(Long medicineId);
}
