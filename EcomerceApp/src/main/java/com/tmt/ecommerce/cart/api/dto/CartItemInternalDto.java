package com.tmt.ecommerce.cart.api.dto;

import java.math.BigDecimal;

public record CartItemInternalDto(
        Long cartItemId,
        Long productVariantId,
        Long shopId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subTotal,
        boolean isAvailable
) {}
