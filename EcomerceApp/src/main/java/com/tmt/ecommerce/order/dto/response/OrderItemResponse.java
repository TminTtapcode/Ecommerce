package com.tmt.ecommerce.order.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record OrderItemResponse(
        Long id,
        Long productVariantId,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subTotal
) {}