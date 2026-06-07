package com.pharmacy.catalog_service.dto;

public record UploadMedicineImageResponse(
        Long medicineId,
        String imageUrl,
        String fileName
) {}
