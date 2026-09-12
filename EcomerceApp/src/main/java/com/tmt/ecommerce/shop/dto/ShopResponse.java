package com.tmt.ecommerce.shop.dto;

import com.tmt.ecommerce.shop.enums.ShopStatus;
import lombok.Builder;

@Builder
public record ShopResponse(
        Long id,
        Long userId,
        String name,
        String description,
        ShopStatus status,
        ShopStatus priorStatus
) {}
