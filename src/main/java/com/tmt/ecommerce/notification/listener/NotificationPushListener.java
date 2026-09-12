package com.tmt.ecommerce.notification.listener;

import com.tmt.ecommerce.notification.service.NotificationWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPushListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void created(NotificationWriter.Created event) {
        try {
            messagingTemplate.convertAndSendToUser(
                    event.recipient().toString(),
                    "/queue/notifications",
                    event.notification()
            );
        } catch (Exception e) {
            log.error("Không thể bắn thông báo realtime cho user {}: {}", event.recipient(), e.getMessage());
        }
    }
}
