package com.tmt.ecommerce.order.api.dto;

import java.math.BigDecimal;

public record RefundableOrderSnapshot(
        long orderId,
        long buyerUserId,
        long shopId,
        String paymentGroupId,
        String paymentMethod,
        String status,
        BigDecimal totalAmount
) {}
