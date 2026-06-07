package com.pharmacy.reporting_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pharmacy.reporting_service.config.JwtTokenHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * REST client gọi sales-service để lấy invoices & items cho báo cáo doanh thu.
 * JWT token forwarded từ ThreadLocal JwtTokenHolder.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SalesClient {

    private final RestTemplate restTemplate;

    @Value("${sales.service.url}")
    private String salesBaseUrl;

    // ── DTOs ──────────────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InvoiceDto(
            Long id,
            String code,
            Long customerId,
            Long cashierId,
            String status,
            String paymentStatus,
            String orderType,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal couponDiscount,
            BigDecimal shippingFee,
            BigDecimal total,
            LocalDateTime createdAt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InvoiceItemDto(
            Long id,
            Long invoiceId,
            Long medicineId,
            String medicineName,
            String unitCode,
            String unitLabel,
            int qty,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentDto(
            Long id,
            Long invoiceId,
            BigDecimal amount,
            String paymentMethod,
            String transactionId,
            String status,
            LocalDateTime paidAt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BuyerOrderDto(
            Long id,
            Long buyerId,
            String buyerName,
            String status,
            String shippingAddress,
            String paymentMethod,
            String paymentStatus,
            String notes,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal total,
            String rejectionReason,
            Long processedBy,
            String stripePaymentIntentId,
            String paymentTransactionId,
            List<BuyerOrderItemDto> items,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BuyerOrderItemDto(
            Long id,
            Long medicineId,
            String medicineName,
            Integer qty,
            String unitCode,
            String unitLabel,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    // ── Methods ───────────────────────────────────────────────────────────────

    public List<InvoiceDto> getInvoices(java.time.LocalDate from, java.time.LocalDate to,
                                         String status, String paymentStatus) {
        try {
            String url = UriComponentsBuilder.fromUriString(salesBaseUrl + "/sales/invoices")
                    .queryParamIfPresent("status", java.util.Optional.ofNullable(status))
                    .queryParamIfPresent("paymentStatus", java.util.Optional.ofNullable(paymentStatus))
                    .queryParamIfPresent("dateFrom", java.util.Optional.ofNullable(
                            from != null ? from.atStartOfDay().toString() : null))
                    .queryParamIfPresent("dateTo", java.util.Optional.ofNullable(
                            to != null ? to.plusDays(1).atStartOfDay().toString() : null))
                    .build().toUriString();

            log.debug("SalesClient.getInvoices url={}", url);
            ResponseEntity<List<InvoiceDto>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<List<InvoiceDto>>() {});

            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("SalesClient.getInvoices failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<InvoiceItemDto> getInvoiceItems(Long invoiceId) {
        try {
            String url = salesBaseUrl + "/sales/invoices/" + invoiceId + "/items";
            ResponseEntity<List<InvoiceItemDto>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<List<InvoiceItemDto>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("SalesClient.getInvoiceItems({}) failed: {}", invoiceId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public InvoiceDto getInvoice(Long invoiceId) {
        try {
            String url = salesBaseUrl + "/sales/invoices/" + invoiceId;
            ResponseEntity<InvoiceDto> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()), InvoiceDto.class);
            return resp.getBody();
        } catch (Exception e) {
            log.warn("SalesClient.getInvoice({}) failed: {}", invoiceId, e.getMessage());
            return null;
        }
    }

    public List<PaymentDto> getInvoicePayments(Long invoiceId) {
        try {
            String url = salesBaseUrl + "/sales/invoices/" + invoiceId + "/payments";
            ResponseEntity<List<PaymentDto>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<List<PaymentDto>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("SalesClient.getInvoicePayments({}) failed: {}", invoiceId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<BuyerOrderDto> getBuyerOrders() {
        try {
            String url = salesBaseUrl + "/pharmacist/orders/all";
            ResponseEntity<List<BuyerOrderDto>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<List<BuyerOrderDto>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("SalesClient.getBuyerOrders failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        String bearer = JwtTokenHolder.bearerHeader();
        if (!bearer.isBlank()) {
            h.set("Authorization", bearer);
        }
        return h;
    }
}
