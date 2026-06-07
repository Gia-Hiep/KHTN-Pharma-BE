package com.pharmacy.sales_service.dto;

/** Request cập nhật số lượng item trong giỏ */
public record UpdateCartItemRequest(
        Integer qty
) {}
