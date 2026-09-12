package com.tmt.ecommerce.order.api.event;

public record OrderConfirmedEvent(
    Long orderId,
    Long buyerId,
    Long shopId,
    String paymentMethod
) {}
