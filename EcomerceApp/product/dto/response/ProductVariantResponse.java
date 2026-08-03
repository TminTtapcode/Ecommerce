package com.tmt.ecommerce.product.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record ProductVariantResponse(
        Long id,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        Map<String, Object> attributes
) {}