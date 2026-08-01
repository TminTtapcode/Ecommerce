package com.tmt.ecommerce.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductCreateRequest(
        @NotNull(message = "ID Cửa hàng không được để trống")
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

        Long brandId, // Brand có thể null nếu sản phẩm không có thương hiệu

        // @Valid cực kỳ quan trọng: Nó báo cho Spring biết phải chui vào bên trong
        // List này và check tiếp các annotation của ProductVariantRequest
        @Valid
        List<ProductVariantRequest> variants,

        @Valid
        List<ProductImageRequest> images

) {}