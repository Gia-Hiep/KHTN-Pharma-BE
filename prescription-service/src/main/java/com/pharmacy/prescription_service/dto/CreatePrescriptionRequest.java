package com.pharmacy.prescription_service.dto;

import java.time.LocalDate;

public record CreatePrescriptionRequest(
        String prescriptionCode,
        String customerPhone,
        String customerName,
        String doctorName,
        String diagnosis,
        LocalDate prescriptionDate,
        String notes
) {}

