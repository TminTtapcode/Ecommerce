package com.tmt.ecommerce.order.service;

import com.tmt.ecommerce.cart.api.CartInternalService;
import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.order.api.event.OrderCancelledEvent;
import com.tmt.ecommerce.order.api.event.OrderConfirmedEvent;
import com.tmt.ecommerce.order.api.event.OrderDeliveredEvent;
import com.tmt.ecommerce.order.api.event.OrderShippedEvent;
import com.tmt.ecommerce.order.dto.request.VendorOrderStatusUpdateRequest;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderItem;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import com.tmt.ecommerce.voucher.api.VoucherInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartInternalService cartInternalService;

    @Mock
    private ProductInternalService productInternalService;

    @Mock
    private ShopInternalService shopInternalService;

    @Mock
    private IdentityInternalService identityInternalService;

    @Mock
    private VoucherInternalService voucherInternalService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = Order.builder()
                .id(1L)
                .userId(10L)
                .shopId(5L)
                .paymentGroupId("PG-12345678")
                .status(OrderStatus.CONFIRMED)
                .shippingAddress("Hà Nội")
                .paymentMethod("COD")
                .totalAmount(new BigDecimal("100000"))
                .originalTotalAmount(new BigDecimal("100000"))
                .discountAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .id(101L)
                .productVariantId(501L)
                .productName("Áo thun polo")
                .quantity(2)
                .unitPrice(new BigDecimal("50000"))
                .subTotal(new BigDecimal("100000"))
                .build();
        sampleOrder.addOrderItem(item);
    }

    @Test
    void updateVendorOrderStatus_ShouldTransitionValidStatus_ConfirmedToShipped() {
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleOrder));
        when(shopInternalService.isShopOwner(5L, 20L)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendorOrderStatusUpdateRequest request = new VendorOrderStatusUpdateRequest(OrderStatus.SHIPPED, null);
        OrderResponse response = orderService.updateVendorOrderStatus(20L, 1L, request);

        assertEquals("SHIPPED", response.status());
        verify(eventPublisher).publishEvent(any(OrderShippedEvent.class));
    }

    @Test
    void updateVendorOrderStatus_ShouldThrowAccessDenied_WhenUserNotShopOwner() {
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleOrder));
        when(shopInternalService.isShopOwner(5L, 99L)).thenReturn(false);

        VendorOrderStatusUpdateRequest request = new VendorOrderStatusUpdateRequest(OrderStatus.SHIPPED, null);
        assertThrows(AccessDeniedException.class, () -> orderService.updateVendorOrderStatus(99L, 1L, request));
    }

    @Test
    void updateVendorOrderStatus_ShouldThrowIllegalArgument_WhenInvalidTransition() {
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleOrder));
        when(shopInternalService.isShopOwner(5L, 20L)).thenReturn(true);

        VendorOrderStatusUpdateRequest request = new VendorOrderStatusUpdateRequest(OrderStatus.DELIVERED, null);
        assertThrows(com.tmt.ecommerce.common.exception.AppException.class, () -> orderService.updateVendorOrderStatus(20L, 1L, request));
    }

    @Test
    void updateVendorOrderStatus_ShouldPublishOrderCancelledEventWithItems_WhenCancelled() {
        sampleOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleOrder));
        when(shopInternalService.isShopOwner(5L, 20L)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendorOrderStatusUpdateRequest request = new VendorOrderStatusUpdateRequest(OrderStatus.CANCELLED, "Hết hàng");
        orderService.updateVendorOrderStatus(20L, 1L, request);

        ArgumentCaptor<OrderCancelledEvent> captor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        OrderCancelledEvent event = captor.getValue();
        assertEquals(1L, event.orderId());
        assertEquals(1, event.items().size());
        assertEquals(501L, event.items().get(0).variantId());
        assertEquals(2, event.items().get(0).quantity());
        verify(productInternalService).restoreStock(501L, 2);
    }
}
