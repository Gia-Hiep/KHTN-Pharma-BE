package com.pharmacy.sales_service.dto;

import java.math.BigDecimal;

public record PayRequest(String paymentMethod, BigDecimal amount, String transactionId) {}
