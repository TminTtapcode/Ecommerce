package com.tmt.ecommerce.payment.api;

public interface PaymentEligibilityQuery {
    boolean hasSuccessfulVnpayPayment(String paymentGroupId);
}
