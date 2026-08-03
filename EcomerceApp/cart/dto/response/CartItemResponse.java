package com.tmt.ecommerce.cart.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record CartItemResponse(
        Long cartItemId,
        Long productVariantId,
        Long shopId, // THÊM TRƯỜNG NÀY ĐỂ TÁCH ĐƠN
        String productName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subTotal, // = unitPrice * quantity
        String thumbnailUrl,
        Map<String, Object> attributes,
        boolean isAvailable // Báo cho FE biết nếu SP hết hàng hoặc bị ẩn
) {}