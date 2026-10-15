package com.tmt.ecommerce.order.job;

import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.order.service.OrderService;
import com.tmt.ecommerce.order.service.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCleanupJob {

    private final OrderRepository orderRepository;
    private final OrderServiceImpl orderService; // We need applyOrderTransition which is public in OrderServiceImpl or we can use adminCancelOrder

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void cancelExpiredVnpayOrders() {
        List<Order> expiredOrders = orderRepository.findExpiredOrders(OrderStatus.PENDING, "VNPAY");
        
        if (!expiredOrders.isEmpty()) {
            log.info("Found {} expired VNPAY orders. Starting cancellation process...", expiredOrders.size());
            for (Order order : expiredOrders) {
                try {
                    orderService.cancelOrderSystem(order.getId());
                } catch (Exception e) {
                    log.error("Failed to auto-cancel order {}: {}", order.getId(), e.getMessage());
                }
            }
        }
    }
}
