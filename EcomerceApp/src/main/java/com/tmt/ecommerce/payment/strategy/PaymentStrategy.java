package com.tmt.ecommerce.payment.strategy;

import com.tmt.ecommerce.payment.dto.request.PaymentOrderData;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import java.util.Map;

public interface PaymentStrategy {

    // Đã thêm tham số txnRef
    String createPaymentUrl(PaymentOrderData orderData, String ipAddress, String txnRef);

    boolean verifyIpnSignature(Map<String, String> params);

    PaymentMethod getPaymentMethod();
}