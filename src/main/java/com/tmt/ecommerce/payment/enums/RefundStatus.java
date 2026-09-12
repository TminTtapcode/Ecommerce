package com.tmt.ecommerce.payment.enums;

/** Refund lifecycle is deliberately independent from PaymentStatus. */
public enum RefundStatus {
    REQUESTED,
    UNKNOWN,
    CONFIRMED,
    REJECTED
}
