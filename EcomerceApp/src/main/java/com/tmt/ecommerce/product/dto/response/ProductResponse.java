package com.tmt.ecommerce.product.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        Long shopId,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        Long categoryId,
        String categoryName,
        String status,
        List<ProductVariantResponse> variants,
        List<ProductImageResponse> imageResponses
) {}
