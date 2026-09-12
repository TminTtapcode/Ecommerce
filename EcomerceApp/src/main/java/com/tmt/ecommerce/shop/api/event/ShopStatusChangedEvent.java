package com.tmt.ecommerce.shop.api.event;

import com.tmt.ecommerce.shop.enums.ShopStatus;

public record ShopStatusChangedEvent(Long actorId, Long shopId, ShopStatus oldStatus,
                                     ShopStatus newStatus, String reason) {}
