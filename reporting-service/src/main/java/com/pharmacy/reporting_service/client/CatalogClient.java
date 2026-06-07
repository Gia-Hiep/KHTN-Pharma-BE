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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST client gọi catalog-service để lấy danh mục thuốc.
 * JWT token forwarded từ ThreadLocal JwtTokenHolder.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogClient {

    private final RestTemplate restTemplate;

    @Value("${catalog.service.url}")
    private String catalogBaseUrl;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MedicineDto(Long id, String name, Long categoryId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CategoryDto(Long id, String name) {}

    public Map<Long, Long> getMedicineCategoryMap() {
        try {
            ResponseEntity<List<MedicineDto>> resp = restTemplate.exchange(
                    catalogBaseUrl + "/catalog/medicines",
                    HttpMethod.GET, new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<List<MedicineDto>>() {});
            List<MedicineDto> meds = resp.getBody();
            if (meds == null) return Collections.emptyMap();
            return meds.stream()
                    .filter(m -> m.categoryId() != null)
                    .collect(Collectors.toMap(MedicineDto::id, MedicineDto::categoryId, (a, b) -> a));
        } catch (Exception e) {
            log.warn("CatalogClient.getMedicineCategoryMap failed: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Map<Long, String> getCategoryNameMap() {
        try {
            ResponseEntity<List<CategoryDto>> resp = restTemplate.exchange(
                    catalogBaseUrl + "/catalog/categories",
                    HttpMethod.GET, new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<List<CategoryDto>>() {});
            List<CategoryDto> cats = resp.getBody();
            if (cats == null) return Collections.emptyMap();
            return cats.stream()
                    .collect(Collectors.toMap(CategoryDto::id, CategoryDto::name, (a, b) -> a));
        } catch (Exception e) {
            log.warn("CatalogClient.getCategoryNameMap failed: {}", e.getMessage());
            return Collections.emptyMap();
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
