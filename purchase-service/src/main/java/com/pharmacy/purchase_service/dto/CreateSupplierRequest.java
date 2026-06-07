package com.pharmacy.purchase_service.dto;

public record CreateSupplierRequest(
        String code,
        String name,
        String contactPerson,
        String phone,
        String email,
        String address,
        String notes
) {}
