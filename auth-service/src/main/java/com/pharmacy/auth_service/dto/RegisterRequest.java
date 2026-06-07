package com.pharmacy.auth_service.dto;

public record RegisterRequest(
        String username,
        String password,
        String fullName,
        String phone,
        String email,
        String address
) {}
