package com.pharmacy.notification_service.controller;

import com.pharmacy.notification_service.dto.CreateNotificationRequest;
import com.pharmacy.notification_service.entity.Notification;
import com.pharmacy.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @Value("${internal.api.key}")
    private String internalApiKey;

    // ═══════════════════════════════════════════════════
    // PUBLIC ENDPOINTS (JWT auth — userId from token)
    // ═══════════════════════════════════════════════════

    /** Paginated notifications for the authenticated user */
    @GetMapping
    public Page<Notification> getNotifications(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = (Long) auth.getPrincipal();
        return service.getNotifications(userId, page, Math.min(size, 50));
    }

    /** Count unread notifications */
    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Map.of("count", service.getUnreadCount(userId));
    }

    /** Mark a single notification as read (validates ownership) */
    @PutMapping("/{id}/read")
    public Notification markAsRead(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return service.markAsRead(userId, id);
    }

    /** Mark all notifications as read */
    @PutMapping("/read-all")
    public Map<String, Object> markAllAsRead(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        int count = service.markAllAsRead(userId);
        return Map.of("marked", count);
    }

    // ═══════════════════════════════════════════════════
    // INTERNAL ENDPOINT (for inter-service calls)
    // ═══════════════════════════════════════════════════

    /**
     * Create notification — INTERNAL ONLY.
     * Secured via X-Internal-Key header (not JWT).
     * Called by sales-service, chat-service, etc.
     */
    @PostMapping("/internal")
    public Notification createInternal(
            @RequestHeader("X-Internal-Key") String apiKey,
            @Valid @RequestBody CreateNotificationRequest req
    ) {
        if (!internalApiKey.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal API key");
        }
        return service.create(req);
    }
}
