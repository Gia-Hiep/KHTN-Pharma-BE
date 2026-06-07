package com.pharmacy.customer_service.repository;

import com.pharmacy.customer_service.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponUsageRepo extends JpaRepository<CouponUsage, Long> {

    List<CouponUsage> findByCouponId(Long couponId);

    List<CouponUsage> findByCustomerId(Long customerId);

    boolean existsByCouponIdAndCustomerId(Long couponId, Long customerId);

    int countByCouponIdAndCustomerId(Long couponId, Long customerId);
}
