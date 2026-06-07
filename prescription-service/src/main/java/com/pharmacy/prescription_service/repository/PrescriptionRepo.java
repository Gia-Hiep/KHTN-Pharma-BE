package com.pharmacy.prescription_service.repository;

import com.pharmacy.prescription_service.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrescriptionRepo extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByPrescriptionCode(String code);


    @Query("""
    select p from Prescription p
    where (:status is null or p.status = :status)
      and (:phone is null or p.customerPhone = :phone)
      and (:createdBy is null or p.createdBy = :createdBy)
      and (:dateFrom is null or p.prescriptionDate >= :dateFrom)
      and (:dateTo is null or p.prescriptionDate <= :dateTo)
      and (
            :q is null or :q = '' or
            lower(p.prescriptionCode) like lower(concat('%', :q, '%')) or
            lower(p.customerPhone) like lower(concat('%', :q, '%')) or
            lower(p.doctorName) like lower(concat('%', :q, '%'))
          )
    order by p.createdAt desc
    """)
    List<Prescription> search(String q, String status, String phone,
                              LocalDate dateFrom, LocalDate dateTo, Long createdBy);
}
