package com.tmt.ecommerce.order.service;

import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.dto.request.VendorOrderStatusUpdateRequest;
import com.tmt.ecommerce.order.dto.response.CheckoutResponse;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.cart.dto.request.CartPreviewRequest;
import com.tmt.ecommerce.cart.dto.response.CartPreviewResponse;
import org.springframework.data.domain.Page;

public interface OrderService {

    CheckoutResponse checkout(Long userId, CheckoutRequest request);

    CartPreviewResponse previewCheckout(Long userId, CartPreviewRequest request);

    Page<OrderResponse> getMyOrders(Long userId, String status, int page, int size);

    OrderResponse getOrderDetail(Long userId, Long orderId);

    OrderResponse cancelOrder(Long userId, Long orderId);

    OrderResponse confirmDelivery(Long userId, Long orderId);

    Page<OrderResponse> getVendorOrders(Long userId, String status, int page, int size);

    OrderResponse getVendorOrderDetail(Long userId, Long orderId);

    OrderResponse updateVendorOrderStatus(Long userId, Long orderId, VendorOrderStatusUpdateRequest request);

    Page<OrderResponse> getAdminOrders(String status, int page, int size);

    OrderResponse getAdminOrderDetail(Long orderId);
}
