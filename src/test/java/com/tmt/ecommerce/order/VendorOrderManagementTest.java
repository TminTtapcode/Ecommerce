package com.tmt.ecommerce.order;

import com.tmt.ecommerce.order.dto.request.VendorOrderStatusUpdateRequest;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.order.service.OrderServiceImpl;
import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class VendorOrderManagementTest {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private ShopInternalService shopInternalService;

    private Long vendorUserId = 100L;
    private Long otherUserId = 999L;
    private Long shopId = 1L;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        when(shopInternalService.getShopIdByUserId(vendorUserId)).thenReturn(shopId);
        when(shopInternalService.isShopOwner(shopId, vendorUserId)).thenReturn(true);
        when(shopInternalService.isShopOwner(shopId, otherUserId)).thenReturn(false);

        testOrder = Order.builder()
                .userId(50L)
                .shopId(shopId)
                .paymentGroupId("PG-TEST-123")
                .status(OrderStatus.CONFIRMED)
                .shippingAddress("123 Test Street")
                .paymentMethod("VNPAY")
                .totalAmount(new BigDecimal("500000"))
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("Vendor hợp lệ cập nhật trạng thái đơn từ CONFIRMED sang SHIPPED")
    void testUpdateStatus_ConfirmedToShipped_Success() {
        VendorOrderStatusUpdateRequest request = new VendorOrderStatusUpdateRequest(OrderStatus.SHIPPED, null);
        OrderResponse response = orderService.updateVendorOrderStatus(vendorUserId, testOrder.getId(), request);

        assertEquals("SHIPPED", response.status());
        Order updatedInDb = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertEquals(OrderStatus.SHIPPED, updatedInDb.getStatus());
    }

    @Test
    @DisplayName("Vendor không được tự cập nhật SHIPPED sang DELIVERED")
    void testUpdateStatus_ShippedToDelivered_ThrowsException() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(testOrder);

        VendorOrderStatusUpdateRequest request = new VendorOrderStatusUpdateRequest(OrderStatus.DELIVERED, null);
        var error = assertThrows(AppException.class,
                () -> orderService.updateVendorOrderStatus(vendorUserId, testOrder.getId(), request));
        assertEquals(ErrorCode.INVALID_ORDER_STATE_TRANSITION, error.getErrorCode());
    }

    @Test
    @DisplayName("Invalid transition returns INVALID_ORDER_STATE_TRANSITION")
    void testUpdateStatus_InvalidTransition_ThrowsException() {
        VendorOrderStatusUpdateRequest request = new VendorOrderStatusUpdateRequest(OrderStatus.DELIVERED, null);

        var error = assertThrows(com.tmt.ecommerce.common.exception.AppException.class, () -> {
            orderService.updateVendorOrderStatus(vendorUserId, testOrder.getId(), request);
        });
        assertEquals(com.tmt.ecommerce.common.exception.ErrorCode.INVALID_ORDER_STATE_TRANSITION, error.getErrorCode());
    }

    @Test
    @DisplayName("User không phải chủ Shop cố tình cập nhật đơn phải ném RuntimeException")
    void testUpdateStatus_NotShopOwner_ThrowsException() {
        VendorOrderStatusUpdateRequest request = new VendorOrderStatusUpdateRequest(OrderStatus.SHIPPED, null);

        assertThrows(RuntimeException.class, () -> {
            orderService.updateVendorOrderStatus(otherUserId, testOrder.getId(), request);
        });
    }
}
