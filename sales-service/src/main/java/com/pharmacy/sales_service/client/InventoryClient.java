package com.pharmacy.sales_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.sales_service.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final RestTemplate rest = buildRestTemplate();

    @Value("${inventory.base-url}")
    private String baseUrl;

    private static RestTemplate buildRestTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.setErrorHandler(new ResponseErrorHandler() {
            private final ObjectMapper om = new ObjectMapper();

            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return response.getStatusCode().isError();
            }

            @Override
            public void handleError(java.net.URI url, org.springframework.http.HttpMethod method,
                                    ClientHttpResponse response) throws IOException {
                String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                String msg;
                try {
                    JsonNode node = om.readTree(body);
                    msg = node.has("message") ? node.get("message").asText() : body;
                } catch (Exception e) {
                    msg = body.isBlank() ? response.getStatusText() : body;
                }
                throw new RuntimeException(msg);
            }
        });
        return rt;
    }


    public InventoryReserveResponse reserve(String bearerToken, InventoryReserveRequest req){
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        h.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(baseUrl + "/inventory/reserve",
                HttpMethod.POST,
                new HttpEntity<>(req, h),
                InventoryReserveResponse.class
        ).getBody();
    }

    public void commit(String bearerToken, String refType, String refId){
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        h.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(baseUrl + "/inventory/commit",
                HttpMethod.POST,
                new HttpEntity<>(new RefRequest(refType, refId), h),
                Void.class);
    }

    public void release(String bearerToken, String refType, String refId){
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        h.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(baseUrl + "/inventory/release",
                HttpMethod.POST,
                new HttpEntity<>(new RefRequest(refType, refId), h),
                Void.class);
    }

    public void returnStock(String bearerToken, String refType, String refId){
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        h.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(baseUrl + "/inventory/return-stock",
                HttpMethod.POST,
                new HttpEntity<>(new RefRequest(refType, refId), h),
                Void.class);
    }

    /**
     * Partially release reservation items — reduce reserved qty per medicine.
     * Used during PICKING when pharmacist reports insufficient stock.
     */
    public void adjustReservation(String bearerToken, String refType, String refId,
                                   java.util.List<AdjustReservationItem> adjustments) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        h.setContentType(MediaType.APPLICATION_JSON);
        rest.exchange(baseUrl + "/inventory/adjust-reservation",
                HttpMethod.POST,
                new HttpEntity<>(java.util.Map.of(
                        "refType", refType,
                        "refId", refId,
                        "adjustments", adjustments
                ), h),
                Void.class);
    }

    /** DTO for adjust-reservation items */
    public record AdjustReservationItem(Long medicineId, int reduceQty) {}

    /**
     * Lấy tổng hợp tồn kho theo medicineId.
     * Trả về danh sách StockSummary (medicineId, onHand, reserved, available).
     */
    public java.util.List<java.util.Map<String, Object>> getStockSummaries(String bearerToken, Long medicineId) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        String url = baseUrl + "/inventory/summary" + (medicineId != null ? "?medicineId=" + medicineId : "");
        var resp = rest.exchange(url, HttpMethod.GET, new HttpEntity<>(h),
                new org.springframework.core.ParameterizedTypeReference<java.util.List<java.util.Map<String, Object>>>() {});
        return resp.getBody() != null ? resp.getBody() : java.util.List.of();
    }
}
