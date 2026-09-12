package com.tmt.ecommerce.payment.gateway;

public interface PaymentRefundGateway {
    RefundGatewayResult refund(RefundGatewayRequest request);
    RefundGatewayResult query(RefundGatewayRequest request);
}
