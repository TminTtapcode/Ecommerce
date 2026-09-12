package com.tmt.ecommerce.payment.gateway;

import java.math.BigDecimal;

/** Data already trusted and persisted by Payment; no HTTP/client values enter this type. */
public record RefundGatewayRequest(
        String merchantRequestId, String txnRef, String originalGatewayTransactionNo,
        String originalTransactionDate, BigDecimal amount, String createBy,
        String serverIp, String orderInfo, boolean fullRefund
) {}
