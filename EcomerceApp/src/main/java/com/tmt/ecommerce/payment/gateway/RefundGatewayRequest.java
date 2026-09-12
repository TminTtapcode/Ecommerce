package com.tmt.ecommerce.payment.gateway;

import java.math.BigDecimal;

public record RefundGatewayRequest(
        String merchantRequestId, String txnRef, String originalGatewayTransactionNo,
        String originalTransactionDate, BigDecimal amount, String createBy,
        String serverIp, String orderInfo, boolean fullRefund
) {}
