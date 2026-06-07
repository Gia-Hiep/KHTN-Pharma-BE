package com.pharmacy.sales_service.dto;

import com.pharmacy.sales_service.entity.BuyerOrder;
import com.pharmacy.sales_service.entity.BuyerOrderItem;
import com.pharmacy.sales_service.entity.OrderStatus;
import com.pharmacy.sales_service.entity.PaymentMethod;
import com.pharmacy.sales_service.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BuyerOrderResponse(
        Long id,
        Long buyerId,
        String buyerName,
        OrderStatus status,
        String shippingAddress,
        // Payment
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        // General
        String notes,
        String couponCode,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        String rejectionReason,
        Long processedBy,
        String stripePaymentIntentId,
        String paymentTransactionId,
        // Shipping info
        String carrier,
        String trackingCode,
        String shipperName,
        String shipperPhone,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        // Return info
        LocalDateTime returnedAt,
        String returnReason,
        Boolean buyerConfirmed,
        // Items & timestamps
        List<ItemDto> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record ItemDto(
            Long id,
            Long medicineId,
            String medicineName,
            Integer qty,
            Integer originalQty,
            String unitCode,
            String unitLabel,
            Integer conversionFactor,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            String priceTier,
            String saleMode,
            Boolean fulfilled
    ) {}

    public static BuyerOrderResponse from(BuyerOrder o) {
        List<ItemDto> items = o.getItems().stream()
                .map(i -> new ItemDto(i.getId(), i.getMedicineId(), i.getMedicineName(),
                        i.getQty(), i.getOriginalQty(), i.getUnitCode(), i.getUnitLabel(),
                        i.getConversionFactor(), i.getUnitPrice(), i.getLineTotal(),
                        i.getPriceTier(), i.getPriceTier(), i.getFulfilled()))
                .toList();
        return new BuyerOrderResponse(
                o.getId(), o.getBuyerId(), o.getBuyerName(), o.getStatus(),
                o.getShippingAddress(),
                o.getPaymentMethod(), o.getPaymentStatus(),
                o.getNotes(), o.getCouponCode(),
                o.getSubtotal(), o.getDiscount(), o.getTotal(),
                o.getRejectionReason(),
                o.getProcessedBy(),
                o.getStripePaymentIntentId(),
                o.getPaymentTransactionId(),
                o.getCarrier(), o.getTrackingCode(),
                o.getShipperName(), o.getShipperPhone(),
                o.getShippedAt(), o.getDeliveredAt(),
                o.getReturnedAt(), o.getReturnReason(),
                o.getBuyerConfirmed(),
                items, o.getCreatedAt(), o.getUpdatedAt()
        );
    }
}
