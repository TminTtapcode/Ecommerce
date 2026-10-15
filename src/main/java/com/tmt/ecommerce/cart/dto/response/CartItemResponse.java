package com.tmt.ecommerce.cart.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        Long productVariantId,
        Long shopId,
        String shopName,
        String productName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subTotal,
        String thumbnailUrl,
        Map<String, Object> attributes,
        boolean isAvailable
) {}
