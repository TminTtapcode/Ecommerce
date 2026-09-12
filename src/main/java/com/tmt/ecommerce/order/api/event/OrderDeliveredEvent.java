package com.tmt.ecommerce.order.api.event;

public record OrderDeliveredEvent(
    Long orderId,
    Long buyerId
) {}
