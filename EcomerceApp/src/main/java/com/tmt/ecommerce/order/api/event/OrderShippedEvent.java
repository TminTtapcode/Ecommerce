package com.tmt.ecommerce.order.api.event;

public record OrderShippedEvent(
    Long orderId,
    Long buyerId
) {}
