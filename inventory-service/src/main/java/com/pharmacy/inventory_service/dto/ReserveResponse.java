package com.pharmacy.inventory_service.dto;

import java.util.List;

public record ReserveResponse(List<Reserved> reservations) {
    public record Reserved(Long medicineId, Long lotId, int qty) {}
}
