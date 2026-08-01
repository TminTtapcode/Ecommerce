package com.tmt.ecommerce.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProductUpdateRequest(
        @NotBlank(message = "Tên sản phẩm không được để trống")
        String name,

        String description,

        @NotNull(message = "Giá không được để trống")
        BigDecimal price,

        @NotNull(message = "Số lượng không được để trống")
        Integer stockQuantity,

        Long categoryId,

        List<ProductVariantUpdateRequest> variants,

        List<ProductImageUpdateRequest> images // Thêm dòng này
) {}