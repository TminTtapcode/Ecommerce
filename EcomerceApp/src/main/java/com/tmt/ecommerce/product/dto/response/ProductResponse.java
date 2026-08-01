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
        String categoryName, // Chỉ trả về tên danh mục cho nhẹ, thay vì cả Object Category
        List<ProductVariantResponse> variants,
        List<ProductImageResponse> imageResponses
) {}