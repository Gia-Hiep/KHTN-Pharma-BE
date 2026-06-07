package com.pharmacy.customer_service.repository;

import com.pharmacy.customer_service.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepo extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    List<Coupon> findByIsActiveTrue();

    List<Coupon> findByCustomerTierOrCustomerTierIsNull(String tier);
}
