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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * REST client gọi inventory-service để lấy tồn kho & sắp hết hạn cho báo cáo.
 * JWT token forwarded từ ThreadLocal JwtTokenHolder.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClient {

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryBaseUrl;

    // ── DTOs (khớp với inventory-service response) ──────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LowStockDto(
            Long medicineId,
            String medicineName,
            long currentQty,
            int threshold
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExpiryAlertDto(
            Long id,
            Long medicineId,
            String medicineName,
            String lotNumber,
            LocalDate expiryDate,
            int qty
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StockSummaryDto(
            Long medicineId,
            String medicineName,
            long totalQty,
            long reservedQty,
            long availableQty
    ) {}

    // ── Methods ───────────────────────────────────────────────────────────────

    public List<LowStockDto> getLowStock() {
        try {
            String url = inventoryBaseUrl + "/inventory/alerts/low-stock";
            log.debug("InventoryClient.getLowStock url={}", url);
            ResponseEntity<List<LowStockDto>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<List<LowStockDto>>() {});
            List<LowStockDto> body = resp.getBody();
            log.debug("InventoryClient.getLowStock returned {} items", body != null ? body.size() : 0);
            return body != null ? body : Collections.emptyList();
        } catch (Exception e) {
            log.warn("InventoryClient.getLowStock failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<ExpiryAlertDto> getExpiringBefore(LocalDate before) {
        try {
            String url = inventoryBaseUrl + "/inventory/alerts/expiry?before=" + before;
            log.debug("InventoryClient.getExpiringBefore url={}", url);
            ResponseEntity<List<ExpiryAlertDto>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<List<ExpiryAlertDto>>() {});
            List<ExpiryAlertDto> body = resp.getBody();
            log.debug("InventoryClient.getExpiringBefore returned {} items", body != null ? body.size() : 0);
            return body != null ? body : Collections.emptyList();
        } catch (Exception e) {
            log.warn("InventoryClient.getExpiringBefore({}) failed: {}", before, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<StockSummaryDto> getSummary() {
        try {
            String url = inventoryBaseUrl + "/inventory/summary";
            log.debug("InventoryClient.getSummary url={}", url);
            ResponseEntity<List<StockSummaryDto>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<List<StockSummaryDto>>() {});
            List<StockSummaryDto> body = resp.getBody();
            log.debug("InventoryClient.getSummary returned {} items", body != null ? body.size() : 0);
            return body != null ? body : Collections.emptyList();
        } catch (Exception e) {
            log.warn("InventoryClient.getSummary failed: {}", e.getMessage());
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
