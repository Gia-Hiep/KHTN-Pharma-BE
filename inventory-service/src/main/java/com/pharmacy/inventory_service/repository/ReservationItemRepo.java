package com.pharmacy.inventory_service.repository;

import com.pharmacy.inventory_service.entity.ReservationItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;

public interface ReservationItemRepo extends JpaRepository<ReservationItem, Long> {

    List<ReservationItem> findByReservationId(Long reservationId);

    @Query(value = "select * from reservation_items where reservation_id = :rid for update", nativeQuery = true)
    List<ReservationItem> findByReservationIdForUpdate(@Param("rid") Long reservationId);
}
