package com.tmt.ecommerce.order.service;

import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.dto.response.OrderItemResponse;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderItem;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.cart.service.CartService;
import com.tmt.ecommerce.cart.dto.response.CartItemResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    @Override
    @Transactional
    public List<OrderResponse> checkout(Long userId, CheckoutRequest request) {

        // 1. Tận dụng hàm getCart có sẵn. (Nếu CartResponse của em là record thì đổi .getItems() thành .items())
        List<CartItemResponse> cartItems = cartService.getCart(userId).items();
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống, không thể thanh toán!");
        }

        // 2. LOGIC TÁCH ĐƠN: Dùng cú pháp của Record (CartItemResponse::shopId thay vì getShopId)
        Map<Long, List<CartItemResponse>> itemsByShop = cartItems.stream()
                .collect(Collectors.groupingBy(CartItemResponse::shopId));

        List<Order> savedOrders = new ArrayList<>();

        // 3. Xử lý từng cụm
        for (Map.Entry<Long, List<CartItemResponse>> entry : itemsByShop.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItemResponse> shopItems = entry.getValue();

            Order order = Order.builder()
                    .userId(userId)
                    .shopId(shopId)
                    .status(OrderStatus.PENDING)
                    .shippingAddress(request.shippingAddress())
                    .paymentMethod(request.paymentMethod())
                    .totalAmount(BigDecimal.ZERO)
                    .build();

            BigDecimal orderTotal = BigDecimal.ZERO;

            for (CartItemResponse cartItem : shopItems) {
                // Gọi data bằng cú pháp Record: cartItem.unitPrice(), cartItem.quantity()
                BigDecimal subTotal = cartItem.unitPrice().multiply(BigDecimal.valueOf(cartItem.quantity()));
                orderTotal = orderTotal.add(subTotal);

                OrderItem orderItem = OrderItem.builder()
                        .productVariantId(cartItem.productVariantId())
                        .productName(cartItem.productName())
                        .unitPrice(cartItem.unitPrice())
                        .quantity(cartItem.quantity())
                        .subTotal(subTotal)
                        .build();

                order.addOrderItem(orderItem);
            }

            order.setTotalAmount(orderTotal);
            savedOrders.add(orderRepository.save(order));
        }

        // 4. Dọn dẹp
        cartService.clearCart(userId);

        // 5. Build DTO trả về
        return savedOrders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productVariantId(item.getProductVariantId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subTotal(item.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .shopId(order.getShopId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}