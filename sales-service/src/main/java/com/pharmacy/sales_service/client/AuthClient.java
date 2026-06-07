package com.pharmacy.sales_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client gọi auth-service để lấy thông tin user (tên) cho backfill đơn cũ.
 */
@Component
@RequiredArgsConstructor
public class AuthClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${auth.base-url}")
    private String baseUrl;

    /**
     * Lấy tên user theo ID qua internal endpoint.
     * Trả về fullName hoặc null nếu mạng lỗi.
     */
    public String getUserName(String bearerToken, Long userId) {
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(bearerToken);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> resp = rest.exchange(
                    baseUrl + "/auth/internal/users/" + userId + "/name",
                    HttpMethod.GET,
                    new HttpEntity<>(h),
                    Map.class
            );
            Map<?, ?> body = resp.getBody();
            if (body != null && body.get("fullName") != null) {
                return body.get("fullName").toString();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
