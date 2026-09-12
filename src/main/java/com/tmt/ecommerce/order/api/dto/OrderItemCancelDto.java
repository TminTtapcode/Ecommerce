package com.tmt.ecommerce.order.api.dto;

public record OrderItemCancelDto(
    Long variantId,
    Integer quantity
) {}
