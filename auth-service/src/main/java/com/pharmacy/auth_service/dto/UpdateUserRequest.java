package com.pharmacy.auth_service.dto;

public record UpdateUserRequest(String fullName, String phone, String email, String address) {}