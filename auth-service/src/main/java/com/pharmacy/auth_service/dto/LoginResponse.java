package com.pharmacy.auth_service.dto;

import java.util.List;

public record LoginResponse(
        String token,
        Long userId,
        List<String> roles
) {}
