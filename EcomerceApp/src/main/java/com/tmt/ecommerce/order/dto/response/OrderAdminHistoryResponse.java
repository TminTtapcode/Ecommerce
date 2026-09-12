package com.tmt.ecommerce.order.dto.response;

import com.tmt.ecommerce.order.entity.OrderAdminStatusHistory;
import java.time.Instant;

public record OrderAdminHistoryResponse(Long id, Long orderId, Long actorUserId,
        String oldStatus, String newStatus, String reason, Instant createdAt) {
    public static OrderAdminHistoryResponse from(OrderAdminStatusHistory h) {
        return new OrderAdminHistoryResponse(h.getId(), h.getOrderId(), h.getActorUserId(),
                h.getOldStatus().name(), h.getNewStatus().name(), h.getReason(), h.getCreatedAt());
    }
}
