package com.pharmacy.catalog_service.repository;

import com.pharmacy.catalog_service.entity.DiseaseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiseaseGroupRepo extends JpaRepository<DiseaseGroup, Long> {

    Optional<DiseaseGroup> findByCode(String code);

    @Query("SELECT d FROM DiseaseGroup d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(d.code) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(d.description, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(d.keywords, '')) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<DiseaseGroup> search(@Param("q") String q);
}
