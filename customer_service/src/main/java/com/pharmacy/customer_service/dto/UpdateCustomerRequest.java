package com.pharmacy.customer_service.dto;

import java.time.LocalDate;

public record UpdateCustomerRequest(
        String fullName,
        String phone,
        String email,
        String address,
        String customerType,
        LocalDate dateOfBirth,
        String gender,
        Integer loyaltyPoints,
        String notes
) {}
