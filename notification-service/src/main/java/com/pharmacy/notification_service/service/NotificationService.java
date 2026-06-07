package com.pharmacy.notification_service.service;

import com.pharmacy.notification_service.dto.CreateNotificationRequest;
import com.pharmacy.notification_service.entity.Notification;
import com.pharmacy.notification_service.repository.NotificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepo repo;

    /** Get paginated notifications for the authenticated user */
    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Long userId, int page, int size) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    /** Count unread notifications */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return repo.countByUserIdAndReadAtIsNull(userId);
    }

    /** Mark a single notification as read — validates ownership */
    @Transactional
    public Notification markAsRead(Long userId, Long notificationId) {
        Notification n = repo.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"));
        if (!n.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập thông báo này");
        }
        if (n.getReadAt() == null) {
            n.setReadAt(LocalDateTime.now());
            return repo.save(n);
        }
        return n;
    }

    /** Mark all notifications as read for a user */
    @Transactional
    public int markAllAsRead(Long userId) {
        return repo.markAllAsRead(userId, LocalDateTime.now());
    }

    /** Create notification (internal — called by other services) */
    @Transactional
    public Notification create(CreateNotificationRequest req) {
        Notification n = Notification.builder()
                .userId(req.userId())
                .type(req.type())
                .title(req.title())
                .message(req.message())
                .referenceId(req.referenceId())
                .build();
        return repo.save(n);
    }
}
