package com.tmt.ecommerce.order.api.dto;

import java.math.BigDecimal;

/** Minimum Order-owned data that Payment needs to evaluate an approved refund command. */
public record RefundableOrderSnapshot(
        long orderId,
        long buyerUserId,
        long shopId,
        String paymentGroupId,
        String paymentMethod,
        String status,
        BigDecimal totalAmount
) {}
