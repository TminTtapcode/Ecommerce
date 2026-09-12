package com.tmt.ecommerce.payment.gateway;

public record RefundGatewayResult(
        Outcome outcome, String responseCode, String transactionStatus,
        String refundTransactionNo, String sanitizedMetadata
) {
    public enum Outcome { CONFIRMED, REJECTED, UNKNOWN }
}
