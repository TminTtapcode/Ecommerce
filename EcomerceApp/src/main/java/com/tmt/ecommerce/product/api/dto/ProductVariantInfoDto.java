package com.tmt.ecommerce.product.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ProductVariantInfoDto(
        Long variantId,
        Long productId,
        Long shopId,
        String productName,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        String status,
        String thumbnailUrl,
        Map<String, Object> attributes
) {
    public ProductVariantInfoDto(
            Long variantId,
            Long shopId,
            String productName,
            String sku,
            BigDecimal price,
            Integer stockQuantity,
            String status,
            String thumbnailUrl,
            Map<String, Object> attributes
    ) {
        this(variantId, null, shopId, productName, sku, price, stockQuantity, status, thumbnailUrl, attributes);
    }
}
