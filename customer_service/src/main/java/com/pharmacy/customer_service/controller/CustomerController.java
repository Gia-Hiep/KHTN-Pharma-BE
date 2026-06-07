package com.pharmacy.customer_service.controller;

import com.pharmacy.customer_service.dto.CreateCustomerRequest;
import com.pharmacy.customer_service.dto.UpdateCustomerRequest;
import com.pharmacy.customer_service.entity.Customer;
import com.pharmacy.customer_service.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    /** Buyer self-lookup: extract userId from JWT, return linked customer */
    @GetMapping("/me")
    public Customer getMyCustomer(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return service.findByUserId(userId);
    }

    @PostMapping
    public Customer create(@RequestBody CreateCustomerRequest req) {
        return service.create(req);
    }

    @GetMapping
    public List<Customer> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/phone/{phone}")
    public Customer getByPhone(@PathVariable String phone) {
        return service.getByPhone(phone);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @RequestBody UpdateCustomerRequest req) {
        return service.update(id, req);
    }
}
