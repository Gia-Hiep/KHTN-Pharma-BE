package com.pharmacy.sales_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CatalogClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${catalog.base-url}")
    private String baseUrl;

    public MedicineDto getMedicineById(String bearerToken, Long id) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);

        ResponseEntity<MedicineDto> resp = rest.exchange(
                baseUrl + "/catalog/medicines/" + id,
                HttpMethod.GET,
                new HttpEntity<>(h),
                MedicineDto.class
        );

        return resp.getBody();
    }

    // field id + salePrice để tính tiền
    public record MedicineDto(Long id, BigDecimal salePrice, String name, String code) {}

    /**
     * Backward-compatible entrypoint.
     */
    public PriceResult calculatePrice(String bearerToken, Long medicineId, String tierCode, int qty) {
        return calculatePrice(bearerToken, medicineId, null, tierCode, qty);
    }

    /**
     * Tính giá cho medicine dựa trên đơn vị bán và số lượng dòng hàng.
     * Gọi catalog-service: GET /catalog/pricing/calculate?medicineId=X&unitCode=Y&saleMode=Z&qty=N
     */
    public PriceResult calculatePrice(String bearerToken, Long medicineId, String unitCode, String saleMode, int qty) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken);
        try {
            StringBuilder url = new StringBuilder(baseUrl)
                    .append("/catalog/pricing/calculate?medicineId=").append(medicineId)
                    .append("&qty=").append(qty)
                    .append("&tierCode=").append(saleMode != null ? saleMode : "RETAIL");
            if (saleMode != null && !saleMode.isBlank()) {
                url.append("&saleMode=").append(saleMode);
            }
            if (unitCode != null && !unitCode.isBlank()) {
                url.append("&unitCode=").append(unitCode);
            }
            ResponseEntity<PriceResult> resp = rest.exchange(
                    url.toString(),
                    HttpMethod.GET,
                    new HttpEntity<>(h),
                    PriceResult.class
            );
            return resp.getBody();
        } catch (Exception e) {
            return null; // Fallback: keep FE price if catalog unreachable
        }
    }

    public record PriceResult(
            Long medicineId,
            String tierCode,
            String saleMode,
            String unitCode,
            String unitLabel,
            Integer conversionFactor,
            Integer qty,
            BigDecimal unitPrice,
            BigDecimal discountPercent
    ) {}
}
