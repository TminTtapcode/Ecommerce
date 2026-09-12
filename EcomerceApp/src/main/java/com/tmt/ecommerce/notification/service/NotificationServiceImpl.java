package com.tmt.ecommerce.notification.service;

import com.tmt.ecommerce.notification.dto.response.NotificationResponse;
import com.tmt.ecommerce.notification.dto.response.UnreadCountResponse;
import com.tmt.ecommerce.notification.entity.Notification;
import com.tmt.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new com.tmt.ecommerce.common.exception.AppException(com.tmt.ecommerce.common.exception.ErrorCode.INVALID_INPUT);
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId, pageable)
                .map(NotificationWriter::response);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return new UnreadCountResponse(count);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new com.tmt.ecommerce.common.exception.AppException(com.tmt.ecommerce.common.exception.ErrorCode.NOT_FOUND));

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

}
