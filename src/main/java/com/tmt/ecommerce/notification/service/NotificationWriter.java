package com.tmt.ecommerce.notification.service;

import com.tmt.ecommerce.notification.dto.response.NotificationResponse;
import com.tmt.ecommerce.notification.entity.Notification;
import com.tmt.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationWriter {

    private final NotificationRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public record Created(Long recipient, NotificationResponse notification) {}

    @Transactional(propagation = Propagation.MANDATORY)
    public void create(Notification notification) {
        if (notification.getId() != null) {
            throw new IllegalArgumentException("New notification required");
        }
        Notification saved = repository.saveAndFlush(notification);
        eventPublisher.publishEvent(new Created(saved.getUserId(), response(saved)));
    }

    public static NotificationResponse response(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType().name(),
                n.getTitle(),
                n.getMessage(),
                n.getReferenceId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
