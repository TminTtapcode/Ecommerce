package com.tmt.ecommerce.payment.dto.response;

import com.tmt.ecommerce.payment.entity.RefundAttempt;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundAttemptResponse(Long id, Long orderId, BigDecimal amount, String currency,
                                    String status, LocalDateTime requestedAt, LocalDateTime resolvedAt) {
    public static RefundAttemptResponse from(RefundAttempt refund) {
        return new RefundAttemptResponse(refund.getId(), refund.getOrderId(), refund.getAmount(), refund.getCurrency(),
                refund.getStatus().name(), refund.getRequestedAt(), refund.getResolvedAt());
    }
}
