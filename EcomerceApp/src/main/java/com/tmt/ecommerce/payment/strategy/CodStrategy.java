package com.tmt.ecommerce.payment.strategy;

import com.tmt.ecommerce.payment.dto.request.PaymentOrderData;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CodStrategy implements PaymentStrategy {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.COD;
    }

    @Override
    public String createPaymentUrl(PaymentOrderData orderData, String ipAddress, String txnRef) {
        return frontendUrl + "/checkout/success?paymentGroupId=" + orderData.paymentGroupId();
    }

    @Override
    public boolean verifyIpnSignature(Map<String, String> params) {
        return true;
    }
}
