package com.tmt.ecommerce.order.repository;

import java.math.BigDecimal;

public interface ShopFulfilledOrderValueProjection {
    long getDeliveredOrderCount();
    BigDecimal getFulfilledGrossOrderValue();
    BigDecimal getVoucherDiscountAmount();
    BigDecimal getFulfilledOrderValue();
}
