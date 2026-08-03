package com.tmt.ecommerce.product.dto.request;

import java.math.BigDecimal;
import java.util.Map;

public record ProductVariantUpdateRequest(
        Long id, // ID có thể null nếu đây là biến thể mới được thêm vào
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        Map<String, Object> attributes
) {}