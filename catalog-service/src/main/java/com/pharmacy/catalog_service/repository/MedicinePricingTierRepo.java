package com.pharmacy.catalog_service.repository;

import com.pharmacy.catalog_service.entity.MedicinePricingTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MedicinePricingTierRepo extends JpaRepository<MedicinePricingTier, Long> {

    List<MedicinePricingTier> findByMedicineId(Long medicineId);

    @Query("SELECT p FROM MedicinePricingTier p " +
           "WHERE p.medicineId = :medicineId " +
           "AND p.tierCode = :tierCode " +
           "AND p.effectiveFrom <= :now " +
           "AND (p.effectiveTo IS NULL OR p.effectiveTo > :now) " +
           "AND p.minQty <= :qty " +
           "ORDER BY p.minQty DESC")
    List<MedicinePricingTier> findActivePricing(
            @Param("medicineId") Long medicineId,
            @Param("tierCode") String tierCode,
            @Param("qty") Integer qty,
            @Param("now") LocalDateTime now
    );

    /**
     * Lấy giá đúng cho tier và số lượng (lấy row đầu tiên = minQty cao nhất phù hợp)
     */
    default Optional<MedicinePricingTier> findBestPrice(Long medicineId, String tierCode, Integer qty) {
        var list = findActivePricing(medicineId, tierCode, qty, LocalDateTime.now());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Query("SELECT p FROM MedicinePricingTier p " +
           "WHERE p.medicineId = :medicineId " +
           "AND p.effectiveFrom <= :now " +
           "AND (p.effectiveTo IS NULL OR p.effectiveTo > :now)")
    List<MedicinePricingTier> findAllActiveByMedicineId(
            @Param("medicineId") Long medicineId,
            @Param("now") LocalDateTime now
    );
}
