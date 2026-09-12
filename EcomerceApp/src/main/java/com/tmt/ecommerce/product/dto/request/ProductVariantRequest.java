package com.tmt.ecommerce.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public record ProductVariantRequest(
        @NotBlank(message = "SKU không được để trống")
        String sku,

        @NotNull(message = "Giá biến thể không được để trống")
        @Min(value = 0, message = "Giá biến thể không được nhỏ hơn 0")
        BigDecimal price,

        @NotNull(message = "Số lượng tồn kho không được để trống")
        @Min(value = 0, message = "Số lượng tồn kho không được nhỏ hơn 0")
        Integer stockQuantity,

        Map<String, Object> attributes
) {}
