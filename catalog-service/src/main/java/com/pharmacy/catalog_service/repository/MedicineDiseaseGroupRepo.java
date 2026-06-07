package com.pharmacy.catalog_service.repository;

import com.pharmacy.catalog_service.entity.MedicineDiseaseGroup;
import com.pharmacy.catalog_service.entity.MedicineDiseaseGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicineDiseaseGroupRepo extends JpaRepository<MedicineDiseaseGroup, MedicineDiseaseGroupId> {

    List<MedicineDiseaseGroup> findByMedicineId(Long medicineId);

    List<MedicineDiseaseGroup> findByDiseaseGroupId(Long diseaseGroupId);

    @Query("SELECT m.medicineId FROM MedicineDiseaseGroup m WHERE m.diseaseGroupId = :diseaseGroupId")
    List<Long> findMedicineIdsByDiseaseGroupId(@Param("diseaseGroupId") Long diseaseGroupId);

    void deleteByMedicineId(Long medicineId);
}
