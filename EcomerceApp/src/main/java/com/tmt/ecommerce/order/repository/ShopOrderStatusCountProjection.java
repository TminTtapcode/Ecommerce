package com.tmt.ecommerce.order.repository;

public interface ShopOrderStatusCountProjection {
    String getStatus();
    long getOrderCount();
}
