package com.pharmacy.purchase_service.dto;

public record ReceiveResponse(
        String purchaseCode,
        String status,
        InboundResponse inbound
) {}
