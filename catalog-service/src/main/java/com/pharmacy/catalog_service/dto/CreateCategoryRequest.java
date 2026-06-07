package com.pharmacy.catalog_service.dto;

public record CreateCategoryRequest(
        String code,
        String name,
        String description
) {}
