package com.pharmacy.catalog_service.dto;

public record MedicineImageResponse(
        Long id,
        Long medicineId,
        String imageUrl,
        String fileName,
        Integer sortOrder,
        boolean isPrimary
) {
    public static MedicineImageResponse from(com.pharmacy.catalog_service.entity.MedicineImage img) {
        return new MedicineImageResponse(
                img.getId(),
                img.getMedicineId(),
                img.getImageUrl(),
                img.getFileName(),
                img.getSortOrder(),
                img.isPrimary()
        );
    }
}
