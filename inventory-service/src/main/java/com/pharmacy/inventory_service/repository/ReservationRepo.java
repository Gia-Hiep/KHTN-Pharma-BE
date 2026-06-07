package com.pharmacy.inventory_service.repository;

import com.pharmacy.inventory_service.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepo extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByRefTypeAndRefId(String refType, String refId);
}
