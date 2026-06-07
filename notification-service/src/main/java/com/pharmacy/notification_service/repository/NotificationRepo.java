package com.pharmacy.notification_service.repository;

import com.pharmacy.notification_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface NotificationRepo extends JpaRepository<Notification, Long> {

    /** Paginated notifications for a specific user, newest first */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** Count unread notifications for a user */
    long countByUserIdAndReadAtIsNull(Long userId);

    /** Mark all as read for a user */
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :now WHERE n.userId = :userId AND n.readAt IS NULL")
    int markAllAsRead(Long userId, LocalDateTime now);
}
