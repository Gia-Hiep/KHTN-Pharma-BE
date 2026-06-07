package com.pharmacy.inventory_service.repository;

import com.pharmacy.inventory_service.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockTransactionRepo extends JpaRepository<StockTransaction, Long> {

    @Query("""
select t from StockTransaction t
where (:refType is null or t.refType = :refType)
  and (:refId is null or t.refId = :refId)
  and (:medicineId is null or t.medicineId = :medicineId)
  and (:type is null or t.type = :type)
  and (:from is null or t.createdAt >= :from)
  and (:to is null or t.createdAt <= :to)
order by t.createdAt desc
""")
    List<StockTransaction> search(@Param("refType") String refType,
                                  @Param("refId") String refId,
                                  @Param("medicineId") Long medicineId,
                                  @Param("type") String type,
                                  @Param("from") java.time.LocalDateTime from,
                                  @Param("to") java.time.LocalDateTime to);
}
