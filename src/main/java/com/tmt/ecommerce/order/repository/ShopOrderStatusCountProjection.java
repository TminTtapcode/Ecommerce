package com.tmt.ecommerce.order.repository;

/** Internal projection owned by the Order persistence layer. */
public interface ShopOrderStatusCountProjection {
    String getStatus();
    long getOrderCount();
}
