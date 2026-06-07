package com.pharmacy.customer_service.dto;

import java.time.LocalDate;

public record CreateCustomerRequest(
        String fullName,
        String phone,
        String email,
        String address,
        LocalDate dateOfBirth,
        String gender,
        String notes
) {}
