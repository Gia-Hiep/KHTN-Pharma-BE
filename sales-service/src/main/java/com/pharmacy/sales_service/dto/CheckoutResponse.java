package com.pharmacy.sales_service.dto;

import com.pharmacy.sales_service.dto.InventoryReserveResponse;

import java.util.List;

public record CheckoutResponse(String invoiceCode, List<InventoryReserveResponse.Reserved> allocations) {
}
