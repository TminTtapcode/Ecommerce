package com.tmt.ecommerce.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProductImageRequest(
        @NotBlank(message = "URL hình ảnh không được để trống")
        String imageUrl,

        String publicId,

        @NotNull(message = "Phải xác định ảnh này có phải là ảnh đại diện hay không")
        Boolean isThumbnail,

        @NotNull(message = "Thứ tự sắp xếp không được để trống")
        Integer sortOrder
) {}
