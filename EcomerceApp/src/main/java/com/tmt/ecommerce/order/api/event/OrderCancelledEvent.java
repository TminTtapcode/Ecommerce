package com.tmt.ecommerce.order.api.event;

import java.util.List;
import com.tmt.ecommerce.order.api.dto.OrderItemCancelDto;

public record OrderCancelledEvent(
    Long orderId,
    Long buyerId,
    Long shopId,
    List<OrderItemCancelDto> items
) {
    public OrderCancelledEvent(Long orderId, Long buyerId, Long shopId) {
        this(orderId, buyerId, shopId, List.of());
    }
}
