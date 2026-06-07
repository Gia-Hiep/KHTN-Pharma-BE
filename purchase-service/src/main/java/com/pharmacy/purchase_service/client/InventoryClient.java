package com.pharmacy.purchase_service.client;

import com.pharmacy.purchase_service.dto.InboundRequest;
import com.pharmacy.purchase_service.dto.InboundResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${inventory.base-url}")
    private String baseUrl; // http://localhost:8084

    public InboundResponse inbound(String bearerToken, InboundRequest req) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        h.setContentType(MediaType.APPLICATION_JSON);

        return rest.exchange(
                baseUrl + "/inventory/inbound",
                HttpMethod.POST,
                new HttpEntity<>(req, h),
                InboundResponse.class
        ).getBody();
    }
}
