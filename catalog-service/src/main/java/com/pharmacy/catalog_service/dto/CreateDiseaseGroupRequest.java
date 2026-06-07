package com.pharmacy.catalog_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDiseaseGroupRequest(
        @NotBlank(message = "code is required")
        String code,

        @NotBlank(message = "name is required")
        String name,

        String description,

        String keywords
) {}
