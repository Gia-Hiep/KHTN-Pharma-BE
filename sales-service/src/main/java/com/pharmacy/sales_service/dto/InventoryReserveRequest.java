package com.pharmacy.sales_service.dto;

import java.util.List;

public record InventoryReserveRequest(String refType, String refId, String strategy, List<Item> items) {
    public record Item(Long medicineId, int qty) {}
}
