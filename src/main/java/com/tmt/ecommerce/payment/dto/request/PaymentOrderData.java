package com.tmt.ecommerce.payment.dto.request;

import com.tmt.ecommerce.payment.enums.PaymentMethod;
import java.math.BigDecimal;

// DTO nội bộ để giao tiếp giữa các module
public record PaymentOrderData(
        String paymentGroupId,
        BigDecimal amount,
        String description,
        PaymentMethod method,
        String vnpayTransactionDate
) {
    public PaymentOrderData(String paymentGroupId, BigDecimal amount, String description, PaymentMethod method) {
        this(paymentGroupId, amount, description, method, null);
    }
}
