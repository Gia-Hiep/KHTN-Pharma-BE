package com.pharmacy.catalog_service.dto;

import java.math.BigDecimal;

public record UpdatePriceRequest(
        BigDecimal newPrice
) {}
