package com.tmt.ecommerce.product.dto.request;

import lombok.Builder;

@Builder
public record ProductImageUpdateRequest(
        Long id, // Có thể null nếu là ảnh thêm mới
        String imageUrl,
        String publicId,
        Boolean isThumbnail,
        Integer sortOrder
) {}