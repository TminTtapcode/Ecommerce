package com.tmt.ecommerce.order.service;

import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.dto.request.VendorOrderStatusUpdateRequest;
import com.tmt.ecommerce.order.dto.response.CheckoutResponse;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.cart.dto.request.CartPreviewRequest;
import com.tmt.ecommerce.cart.dto.response.CartPreviewResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    // Trả về CheckoutResponse chứa paymentGroupId và danh sách đơn hàng được tách (Multi-vendor)
    CheckoutResponse checkout(Long userId, CheckoutRequest request);

    CartPreviewResponse previewCheckout(Long userId, CartPreviewRequest request);

    // Lấy danh sách lịch sử đơn hàng của User đang đăng nhập
    Page<OrderResponse> getMyOrders(Long userId, String status, int page, int size);

    // Lấy chi tiết đơn hàng của User đang đăng nhập
    OrderResponse getOrderDetail(Long userId, Long orderId);

    // === VENDOR ORDER MANAGEMENT ===

    // Lấy danh sách đơn hàng thuộc Shop của Vendor
    Page<OrderResponse> getVendorOrders(Long userId, String status, int page, int size);

    // Lấy chi tiết đơn hàng thuộc Shop (kiểm tra quyền sở hữu Shop)
    OrderResponse getVendorOrderDetail(Long userId, Long orderId);

    // Vendor cập nhật trạng thái đơn hàng (với State Machine validation)
    OrderResponse updateVendorOrderStatus(Long userId, Long orderId, VendorOrderStatusUpdateRequest request);
}