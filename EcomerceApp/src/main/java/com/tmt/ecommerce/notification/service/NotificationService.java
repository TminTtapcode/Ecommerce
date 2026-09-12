package com.tmt.ecommerce.notification.service;

import com.tmt.ecommerce.notification.dto.response.NotificationResponse;
import com.tmt.ecommerce.notification.dto.response.UnreadCountResponse;
import org.springframework.data.domain.Page;

public interface NotificationService {
    Page<NotificationResponse> getUserNotifications(Long userId, int page, int size);
    UnreadCountResponse getUnreadCount(Long userId);
    void markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
}
