package com.pharmacy.sales_service.dto;

import java.util.List;

public record InventoryReserveResponse(List<Reserved> reservations) {
    public record Reserved(Long medicineId, Long lotId, int qty) {}
}
