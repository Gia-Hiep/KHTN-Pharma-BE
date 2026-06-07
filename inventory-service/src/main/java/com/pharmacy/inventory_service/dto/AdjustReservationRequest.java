package com.pharmacy.inventory_service.dto;

import java.util.List;

/**
 * Request for partial release of reservation items.
 * Used during PICKING step when pharmacist reports insufficient stock.
 */
public record AdjustReservationRequest(
        String refType,
        String refId,
        List<Adjustment> adjustments
) {
    public record Adjustment(
            Long medicineId,
            int reduceQty       // quantity to release from reservation
    ) {}
}
