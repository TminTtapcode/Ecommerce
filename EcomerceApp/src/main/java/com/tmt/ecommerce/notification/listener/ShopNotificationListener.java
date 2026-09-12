package com.tmt.ecommerce.notification.listener;

import com.tmt.ecommerce.common.service.EmailService;
import com.tmt.ecommerce.shop.api.event.ShopApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopNotificationListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleShopApprovedEvent(ShopApprovedEvent event) {
        log.info("Received ShopApprovedEvent for shop: {}, dispatching approval email to: {}", event.shopId(), event.ownerEmail());
        try {
            emailService.sendShopApprovalNotification(event.ownerEmail(), event.shopName());
        } catch (Exception e) {
            log.error("Failed to dispatch shop approval notification email for shop: {}. Error: {}", event.shopId(), e.getMessage());
        }
    }
}
