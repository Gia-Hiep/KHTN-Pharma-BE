package com.pharmacy.catalog_service.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AssignDiseaseGroupsRequest(
        @NotEmpty(message = "diseaseGroupIds is required")
        List<Long> diseaseGroupIds
) {}
