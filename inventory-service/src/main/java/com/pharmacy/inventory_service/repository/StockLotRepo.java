package com.pharmacy.inventory_service.repository;

import com.pharmacy.inventory_service.entity.StockLot;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface StockLotRepo extends JpaRepository<StockLot, Long> {

    @Query(value = """
    select *
    from stock_lots
    where medicine_id = :medicineId
      and (qty_on_hand - qty_reserved) > 0
      and expiry_date > curdate()
    order by expiry_date asc
    for update
  """, nativeQuery = true)
    List<StockLot> findAvailableLotsFefoForUpdate(@Param("medicineId") Long medicineId);

    @Query(value = "select * from stock_lots where id = :id for update", nativeQuery = true)
    Optional<StockLot> findByIdForUpdate(@Param("id") Long id);
    Optional<StockLot> findByMedicineIdAndLotNumberAndExpiryDate(Long medicineId, String lotNumber, java.time.LocalDate expiryDate);

    List<StockLot> findByMedicineIdOrderByExpiryDateAsc(Long medicineId);

    @Query("""
select l from StockLot l
where (:medicineId is null or l.medicineId = :medicineId)
  and (:expBefore is null or l.expiryDate <= :expBefore)
order by l.expiryDate asc
""")
    List<StockLot> findLots(@Param("medicineId") Long medicineId,
                            @Param("expBefore") java.time.LocalDate expBefore);

    @Query("""
            select l.medicineId as medicineId,
                   max(l.medicineName) as medicineName,
                   sum(l.qtyOnHand) as onHand,
                   sum(l.qtyReserved) as reserved
            from StockLot l
            where l.expiryDate > CURRENT_DATE
            group by l.medicineId
            """)
    List<Object[]> summaryAll();

    @Query("""
select l.medicineId as medicineId,
       max(l.medicineName) as medicineName,
       sum(l.qtyOnHand) as onHand,
       sum(l.qtyReserved) as reserved
from StockLot l
where l.medicineId = :medicineId
  and l.expiryDate > CURRENT_DATE
group by l.medicineId
""")
    List<Object[]> summaryOne(@Param("medicineId") Long medicineId);

}
