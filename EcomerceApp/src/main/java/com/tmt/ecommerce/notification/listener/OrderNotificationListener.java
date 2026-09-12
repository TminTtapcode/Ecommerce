package com.tmt.ecommerce.notification.listener;

import com.tmt.ecommerce.notification.entity.Notification;
import com.tmt.ecommerce.notification.entity.NotificationType;
import com.tmt.ecommerce.notification.repository.NotificationRepository;
import com.tmt.ecommerce.order.api.event.OrderCancelledEvent;
import com.tmt.ecommerce.order.api.event.OrderConfirmedEvent;
import com.tmt.ecommerce.order.api.event.OrderDeliveredEvent;
import com.tmt.ecommerce.order.api.event.OrderShippedEvent;
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
public class OrderNotificationListener {

    private final NotificationRepository notificationRepository;
    private final com.tmt.ecommerce.notification.service.NotificationWriter writer;
    private final ShopInternalService shopInternalService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        try {
            log.info("Received OrderConfirmedEvent for order: {}", event.orderId());

            if (!notificationRepository.existsByUserIdAndTypeAndReferenceId(event.buyerId(), NotificationType.ORDER_CONFIRMED, String.valueOf(event.orderId()))) {
                String method = event.paymentMethod() != null ? event.paymentMethod() : "N/A";
                Notification buyerNotification = Notification.builder()
                        .userId(event.buyerId())
                        .type(NotificationType.ORDER_CONFIRMED)
                        .title("Đơn hàng đã được xác nhận")
                        .message("Đơn hàng #" + event.orderId() + " của bạn đã được xác nhận (Thanh toán: " + method + ") và đang chờ shop chuẩn bị hàng.")
                        .referenceId(String.valueOf(event.orderId()))
                        .build();
                writer.create(buyerNotification);
            }

            Optional<Long> vendorUserIdOpt = shopInternalService.getUserIdByShopId(event.shopId());
            if (vendorUserIdOpt.isPresent()) {
                Long vendorUserId = vendorUserIdOpt.get();
                if (!notificationRepository.existsByUserIdAndTypeAndReferenceId(vendorUserId, NotificationType.NEW_ORDER, String.valueOf(event.orderId()))) {
                    Notification vendorNotification = Notification.builder()
                            .userId(vendorUserId)
                            .type(NotificationType.NEW_ORDER)
                            .title("Bạn có đơn hàng mới")
                            .message("Shop của bạn vừa nhận được đơn hàng mới #" + event.orderId() + ".")
                            .referenceId(String.valueOf(event.orderId()))
                            .build();
                    writer.create(vendorNotification);
                }
            } else {
                log.warn("Cannot send NEW_ORDER notification: Shop {} does not exist or has no owner.", event.shopId());
            }

        } catch (Exception e) {
            log.error("Failed to process OrderConfirmedEvent for order {}. Error: {}", event.orderId(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderShipped(OrderShippedEvent event) {
        try {
            log.info("Received OrderShippedEvent for order: {}", event.orderId());

            Notification buyerNotification = Notification.builder()
                    .userId(event.buyerId())
                    .type(NotificationType.ORDER_SHIPPED)
                    .title("Đơn hàng đang được giao")
                    .message("Đơn hàng #" + event.orderId() + " của bạn đã được giao cho đơn vị vận chuyển.")
                    .referenceId(String.valueOf(event.orderId()))
                    .build();
            writer.create(buyerNotification);
        } catch (Exception e) {
            log.error("Failed to process OrderShippedEvent for order {}. Error: {}", event.orderId(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        try {
            log.info("Received OrderDeliveredEvent for order: {}", event.orderId());

            Notification buyerNotification = Notification.builder()
                    .userId(event.buyerId())
                    .type(NotificationType.ORDER_DELIVERED)
                    .title("Đơn hàng giao thành công")
                    .message("Đơn hàng #" + event.orderId() + " của bạn đã được giao thành công.")
                    .referenceId(String.valueOf(event.orderId()))
                    .build();
            writer.create(buyerNotification);
        } catch (Exception e) {
            log.error("Failed to process OrderDeliveredEvent for order {}. Error: {}", event.orderId(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        try {
            log.info("Received OrderCancelledEvent for order: {}", event.orderId());

        } catch (Exception e) {
            log.error("Failed to process OrderCancelledEvent for order {}. Error: {}", event.orderId(), e.getMessage());
        }
    }
}
