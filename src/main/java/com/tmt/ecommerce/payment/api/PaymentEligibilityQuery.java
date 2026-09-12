package com.tmt.ecommerce.payment.api;

/** Committed positive evidence only; never locks Payment while a caller holds Order. */
public interface PaymentEligibilityQuery {
    boolean hasSuccessfulVnpayPayment(String paymentGroupId);
}
