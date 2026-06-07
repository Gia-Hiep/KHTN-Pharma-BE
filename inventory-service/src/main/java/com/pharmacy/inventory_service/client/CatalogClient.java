package com.pharmacy.inventory_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class CatalogClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${catalog.base-url}")
    private String baseUrl;

    public MedicineDto getMedicineById(String bearerToken, Long id) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);

        try {
            ResponseEntity<MedicineDto> resp = rest.exchange(
                    baseUrl + "/catalog/medicines/" + id,
                    HttpMethod.GET,
                    new HttpEntity<>(h),
                    MedicineDto.class
            );
            return resp.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public record MedicineDto(Long id, String name, String code) {}
}
