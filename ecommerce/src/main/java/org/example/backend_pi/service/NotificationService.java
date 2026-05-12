package org.example.backend_pi.service;


// service/NotificationService.java + impl

import org.example.backend_pi.entity.Notification;
import org.example.backend_pi.enums.NotificationType;
import org.example.backend_pi.repository.NotificationRepository;

import java.util.List;

// ─── INTERFACE ───────────────────────────────────────────────
public interface NotificationService {
    Notification create(Long userId, NotificationType type, String title, String content,
                        Long entityId, String entityType, String actionUrl);
    List<Notification> getByUserId(Long userId);
    List<Notification> getUnreadByUserId(Long userId);
    long countUnread(Long userId);
    Notification markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    void deleteNotification(Long notificationId);
}

