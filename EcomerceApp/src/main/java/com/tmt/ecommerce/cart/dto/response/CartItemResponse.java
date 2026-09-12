package com.tmt.ecommerce.cart.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record CartItemResponse(
        Long cartItemId,
        Long productVariantId,
        Long shopId,
        String productName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subTotal,
        String thumbnailUrl,
        Map<String, Object> attributes,
        boolean isAvailable
) {}
