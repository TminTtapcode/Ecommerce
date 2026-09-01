package com.tmt.ecommerce.order.api.dto;

import java.math.BigDecimal;

public record OrderPaymentDto(
        String paymentGroupId,
        BigDecimal amount,
        String description,
        String method
) {}
