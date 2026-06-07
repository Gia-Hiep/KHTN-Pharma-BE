package com.pharmacy.catalog_service.repository;

import com.pharmacy.catalog_service.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineRepo extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByCode(String code);
    Optional<Medicine> findByBarcode(String barcode);

    @Query("""
    select m from Medicine m
    where lower(m.name) like lower(concat('%', :q, '%'))
       or lower(coalesce(m.genericName, '')) like lower(concat('%', :q, '%'))
       or lower(coalesce(m.activeIngredient, '')) like lower(concat('%', :q, '%'))
       or lower(coalesce(m.description, '')) like lower(concat('%', :q, '%'))
       or lower(coalesce(m.dosageForm, '')) like lower(concat('%', :q, '%'))
       or lower(coalesce(m.packageSize, '')) like lower(concat('%', :q, '%'))
       or lower(coalesce(m.manufacturer, '')) like lower(concat('%', :q, '%'))
       or lower(m.code) like lower(concat('%', :q, '%'))
       or lower(coalesce(m.barcode, '')) like lower(concat('%', :q, '%'))
  """)
    List<Medicine> search(@Param("q") String q);
}
