package com.pharmacy.sales_service.dto;

public record CreateInvoiceRequest(
        String code,
        String customerPhone,
        String customerName,
        Long customerId
) {}
