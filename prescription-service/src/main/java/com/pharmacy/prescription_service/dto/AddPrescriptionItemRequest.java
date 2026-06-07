package com.pharmacy.prescription_service.dto;

public record AddPrescriptionItemRequest(
        Long medicineId,
        int quantity,
        String dosageInstructions
) {}
