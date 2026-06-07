package com.pharmacy.inventory_service.dto;

import java.util.List;

public record ReserveRequest(
        String refType,   // INVOICE
        String refId,     // invoice_code
        String strategy,  // FEFO
        List<Item> items
) {
    public record Item(Long medicineId, int qty) {}
}
