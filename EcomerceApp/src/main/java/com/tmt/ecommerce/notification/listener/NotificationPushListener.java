package com.tmt.ecommerce.notification.listener;

import com.tmt.ecommerce.notification.service.NotificationWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPushListener {
    private final SimpMessagingTemplate messaging;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void created(NotificationWriter.Created event) {
        try { messaging.convertAndSendToUser(event.recipient().toString(), "/queue/notifications", event.notification()); }
        catch (Exception failure) {
            log.error("event=notification_push_failed notificationId={} failureType={}", event.notification().id(), failure.getClass().getSimpleName());
        }
    }
}
