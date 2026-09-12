package com.tmt.ecommerce.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductCreateRequest(
        Long shopId,

        @NotBlank(message = "Tên sản phẩm không được để trống")
        @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
        String name,

        String description,

        @NotNull(message = "Giá gốc không được để trống")
        @Min(value = 0, message = "Giá gốc không được nhỏ hơn 0")
        BigDecimal price,

        @NotNull(message = "Tổng tồn kho không được để trống")
        @Min(value = 0, message = "Tổng tồn kho không được nhỏ hơn 0")
        Integer stockQuantity,

        @NotNull(message = "Danh mục sản phẩm không được để trống")
        Long categoryId,

        Long brandId,

        @Valid
        List<ProductVariantRequest> variants,

        @Valid
        List<ProductImageRequest> images

) {}
