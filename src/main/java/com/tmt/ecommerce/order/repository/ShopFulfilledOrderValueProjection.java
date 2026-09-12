package com.tmt.ecommerce.order.repository;

import java.math.BigDecimal;

/** Internal projection owned by the Order persistence layer. */
public interface ShopFulfilledOrderValueProjection {
    long getDeliveredOrderCount();
    BigDecimal getFulfilledGrossOrderValue();
    BigDecimal getVoucherDiscountAmount();
    BigDecimal getFulfilledOrderValue();
}
