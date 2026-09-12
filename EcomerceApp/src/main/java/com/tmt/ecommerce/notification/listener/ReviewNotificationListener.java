package com.tmt.ecommerce.notification.listener;

import com.tmt.ecommerce.notification.entity.Notification;
import com.tmt.ecommerce.notification.entity.NotificationType;
import com.tmt.ecommerce.notification.repository.NotificationRepository;
import com.tmt.ecommerce.review.api.event.ReviewCreatedEvent;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewNotificationListener {

    private final NotificationRepository notificationRepository;
    private final com.tmt.ecommerce.notification.service.NotificationWriter writer;
    private final ShopInternalService shopInternalService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        try {
            log.info("Received ReviewCreatedEvent for review: {}", event.reviewId());

            Optional<Long> vendorUserIdOpt = shopInternalService.getUserIdByShopId(event.shopId());
            if (vendorUserIdOpt.isPresent()) {
                Notification vendorNotification = Notification.builder()
                        .userId(vendorUserIdOpt.get())
                        .type(NotificationType.NEW_REVIEW)
                        .title("Đánh giá sản phẩm mới")
                        .message("Sản phẩm của bạn vừa nhận được 1 đánh giá " + event.rating() + " sao.")
                        .referenceId(String.valueOf(event.reviewId()))
                        .build();
                writer.create(vendorNotification);
            } else {
                log.warn("Cannot send NEW_REVIEW notification: Shop {} does not exist or has no owner.", event.shopId());
            }
        } catch (Exception e) {
            log.error("Failed to process ReviewCreatedEvent for review {}. Error: {}", event.reviewId(), e.getMessage());
        }
    }
}
