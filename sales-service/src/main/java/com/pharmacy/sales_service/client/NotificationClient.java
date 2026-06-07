package com.pharmacy.sales_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client gọi notification-service để tạo thông báo cho user.
 * Gọi endpoint internal, sử dụng X-Internal-Key (không dùng JWT).
 * Tất cả cuộc gọi đều async — notification failure KHÔNG block order flow.
 */
@Slf4j
@Component
public class NotificationClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${notification.base-url:http://localhost:8092}")
    private String baseUrl;

    @Value("${notification.internal-key:PHARMACY_INTERNAL_KEY_2024}")
    private String internalKey;

    /**
     * Gửi notification tới user — async, fire-and-forget.
     */
    @Async
    public void sendNotification(Long userId, String type, String title, String message, Long referenceId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Key", internalKey);

            Map<String, Object> body = Map.of(
                    "userId", userId,
                    "type", type != null ? type : "SYSTEM",
                    "title", title != null ? title : "",
                    "message", message != null ? message : "",
                    "referenceId", referenceId != null ? referenceId : 0L
            );

            rest.exchange(
                    baseUrl + "/notifications/internal",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            log.debug("Notification sent: userId={}, type={}, ref={}", userId, type, referenceId);
        } catch (Exception e) {
            log.warn("Failed to send notification (non-blocking): {}", e.getMessage());
        }
    }

    /** Convenience: order status notification */
    public void notifyOrderStatus(Long buyerId, Long orderId, String title, String message) {
        sendNotification(buyerId, "ORDER_STATUS", title, message, orderId);
    }
}
