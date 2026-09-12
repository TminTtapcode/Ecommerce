package com.tmt.ecommerce.order.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponse(
        Long id,
        Long userId,
        String customerName,
        Long shopId,
        String status,
        BigDecimal totalAmount,
        String shippingAddress,
        String paymentMethod,
        LocalDateTime createdAt,
        LocalDateTime deliveredAt,
        String deliveryConfirmationSource,
        List<OrderItemResponse> items
) {}
