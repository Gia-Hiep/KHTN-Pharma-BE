package com.pharmacy.auth_service.dto;

import java.util.List;

public record CreateUserRequest(
        String username, String password, String fullName,
        String phone, String email, String address,
        List<String> roles
) {}
