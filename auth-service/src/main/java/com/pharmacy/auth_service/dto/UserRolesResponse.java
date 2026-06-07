package com.pharmacy.auth_service.dto;

import java.util.List;

public record UserRolesResponse(Long userId, List<String> roles) {}
