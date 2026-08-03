package com.tmt.ecommerce.order.service;

import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    // Trả về một List vì 1 lần checkout có thể sinh ra nhiều đơn hàng (Multi-vendor)
    List<OrderResponse> checkout(Long userId, CheckoutRequest request);
}