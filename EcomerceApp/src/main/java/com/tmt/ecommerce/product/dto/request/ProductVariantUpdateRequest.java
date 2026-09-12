package com.tmt.ecommerce.product.dto.request;

import java.math.BigDecimal;
import java.util.Map;

public record ProductVariantUpdateRequest(
        Long id,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        Map<String, Object> attributes
) {}
