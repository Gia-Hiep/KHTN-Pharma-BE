package com.pharmacy.auth_service.dto;

import java.util.List;

public record UserDetailResponse(
        Long id, String username, String fullName, String phone, String email,
        String address, Boolean active, List<String> roles
) {}
