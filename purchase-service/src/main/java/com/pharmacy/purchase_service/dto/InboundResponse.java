package com.pharmacy.purchase_service.dto;

import java.util.List;

public record InboundResponse(
        String refType,
        String refId,
        List<Result> results
) {
    public record Result(
            Long medicineId,
            Long lotId,
            String lotNumber,
            int addedQty,
            int qtyOnHandAfter
    ) {}
}
