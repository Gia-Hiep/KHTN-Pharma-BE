package com.pharmacy.customer_service.service;

import com.pharmacy.customer_service.dto.CreateCustomerRequest;
import com.pharmacy.customer_service.dto.UpdateCustomerRequest;
import com.pharmacy.customer_service.entity.Customer;
import com.pharmacy.customer_service.repository.CustomerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepo repo;

    @Transactional
    public Customer create(CreateCustomerRequest req) {
        if (repo.existsByPhone(req.phone())) {
            throw new RuntimeException("Phone already exists");
        }

        Customer c = new Customer();
        c.setFullName(req.fullName());
        c.setPhone(req.phone());
        c.setEmail(req.email());
        c.setAddress(req.address());
        c.setDateOfBirth(req.dateOfBirth());
        c.setGender(req.gender());
        c.setNotes(req.notes());
        c.setLoyaltyPoints(0);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return repo.save(c);
    }

    public Customer get(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public Customer getByPhone(String phone) {
        return repo.findByPhone(phone).orElseThrow();
    }

    public List<Customer> list() {
        return repo.findAll();
    }

    /**
     * Find customer by linked auth userId.
     * Auto-creates a basic customer record if none exists (for new buyers).
     */
    @Transactional
    public Customer findByUserId(Long userId) {
        return repo.findByUserId(userId).orElseGet(() -> {
            Customer c = new Customer();
            c.setUserId(userId);
            c.setFullName("Buyer #" + userId);
            c.setPhone("TEMP_" + userId); // Placeholder until buyer updates profile
            c.setLoyaltyPoints(0);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return repo.save(c);
        });
    }

    @Transactional
    public Customer update(Long id, UpdateCustomerRequest req) {
        Customer c = repo.findById(id).orElseThrow();
        if (req.fullName() != null) c.setFullName(req.fullName());
        if (req.phone() != null && !req.phone().isBlank()) {
            // Only update phone if it changed
            if (!req.phone().equals(c.getPhone())) {
                if (repo.existsByPhone(req.phone())) {
                    throw new RuntimeException("Phone already exists: " + req.phone());
                }
                c.setPhone(req.phone());
            }
        }
        if (req.email() != null) c.setEmail(req.email());
        if (req.address() != null) c.setAddress(req.address());
        if (req.customerType() != null) c.setCustomerType(req.customerType());
        if (req.dateOfBirth() != null) c.setDateOfBirth(req.dateOfBirth());
        if (req.gender() != null) c.setGender(req.gender());
        if (req.loyaltyPoints() != null) c.setLoyaltyPoints(req.loyaltyPoints());
        if (req.notes() != null) c.setNotes(req.notes());
        c.setUpdatedAt(LocalDateTime.now());
        return repo.save(c);
    }
}
