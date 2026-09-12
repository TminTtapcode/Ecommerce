package com.tmt.ecommerce.product.dto.request;

import lombok.Builder;

@Builder
public record ProductImageUpdateRequest(
        Long id,
        String imageUrl,
        String publicId,
        Boolean isThumbnail,
        Integer sortOrder
) {}
