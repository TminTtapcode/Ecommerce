package com.tmt.ecommerce.product.dto.response;

import lombok.Builder;

@Builder
public record ProductImageResponse(
        Long id,
        String imageUrl,
        String publicId,
        boolean isThumbnail,
        int sortOrder
) {}
