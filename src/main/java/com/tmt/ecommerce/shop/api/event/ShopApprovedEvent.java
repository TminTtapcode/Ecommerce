package com.tmt.ecommerce.shop.api.event;

public record ShopApprovedEvent(
        Long shopId,
        Long ownerUserId,
        String ownerEmail,
        String shopName
) {}
