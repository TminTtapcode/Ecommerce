package com.tmt.ecommerce.product.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ProductVariantInfoDto(
        Long variantId,
        Long shopId,
        String productName,
        String sku,
        BigDecimal price, // Giá mới nhất
        Integer stockQuantity,
        String status,
        String thumbnailUrl,
        Map<String, Object> attributes
) {}