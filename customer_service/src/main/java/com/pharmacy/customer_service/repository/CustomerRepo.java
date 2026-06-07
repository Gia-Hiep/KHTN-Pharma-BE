package com.pharmacy.customer_service.repository;

import com.pharmacy.customer_service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepo extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Optional<Customer> findByUserId(Long userId);
}
