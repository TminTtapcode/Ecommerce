package com.tmt.ecommerce.review.api.event;

public record ReviewCreatedEvent(
    Long reviewId,
    Long buyerId,
    Long shopId,
    Long productId,
    int rating
) {}
