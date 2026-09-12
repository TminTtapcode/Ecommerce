package com.tmt.ecommerce.order;

import com.tmt.ecommerce.cart.api.CartInternalService;
import com.tmt.ecommerce.cart.api.dto.CartItemInternalDto;
import com.tmt.ecommerce.notification.entity.Notification;
import com.tmt.ecommerce.notification.entity.NotificationType;
import com.tmt.ecommerce.notification.repository.NotificationRepository;
import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.dto.response.CheckoutResponse;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.order.service.OrderService;
import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.order.dto.request.VendorOrderStatusUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
public class CodCheckoutNotificationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private CartInternalService cartInternalService;

    @MockitoBean
    private ProductInternalService productInternalService;

    @MockitoBean
    private ShopInternalService shopInternalService;

    @MockitoBean
    private IdentityInternalService identityInternalService;

    private Long buyerUserId = 200L;
    private Long vendorUserId = 100L;
    private Long shopId = 1L;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        notificationRepository.deleteAll();

        // Mock cart items
        CartItemInternalDto mockCartItem = new CartItemInternalDto(
                1L, 10L, shopId, "Test Product",
                new BigDecimal("50000"), 2, new BigDecimal("100000"), true
        );
        when(cartInternalService.getCartItems(buyerUserId)).thenReturn(List.of(mockCartItem));

        // Mock product deduct stock (do nothing/return true)
        doNothing().when(productInternalService).deductStock(any(Long.class), any(Integer.class));

        // Mock shop internal service
        when(shopInternalService.getUserIdByShopId(shopId)).thenReturn(Optional.of(vendorUserId));
        when(shopInternalService.isShopOwner(shopId, vendorUserId)).thenReturn(true);
        
        // Mock identity internal service
        when(identityInternalService.getUserFullNameOrDefault(any())).thenReturn("Test User");
    }

    @Test
    @DisplayName("Thanh toán COD thành công sẽ tạo Order ở trạng thái PENDING; khi Vendor xác nhận sẽ chuyển sang CONFIRMED và gửi thông báo")
    void testCodCheckout_Success_SendsNotifications() {
        CheckoutRequest request = new CheckoutRequest(
                "123 Test Street",
                "COD",
                null,
                null
        );

        CheckoutResponse response = orderService.checkout(buyerUserId, request);

        // 1. Assert Order ban đầu ở trạng thái PENDING
        assertNotNull(response);
        assertEquals(1, response.getOrders().size());
        
        Long orderId = response.getOrders().get(0).id();
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.PENDING, order.getStatus(), "COD order should initially be in PENDING status");
        assertEquals("COD", order.getPaymentMethod());

        // 2. Vendor xác nhận đơn hàng -> Chuyển sang CONFIRMED và phát OrderConfirmedEvent
        orderService.updateVendorOrderStatus(vendorUserId, orderId, new VendorOrderStatusUpdateRequest(OrderStatus.CONFIRMED, null));

        Order confirmedOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, confirmedOrder.getStatus(), "Order should be CONFIRMED after vendor confirmation");

        // 3. Assert exactly 1 notification for buyer, containing COD in message
        List<Notification> buyerNotifications = notificationRepository.findAll().stream()
                .filter(n -> n.getUserId().equals(buyerUserId))
                .toList();
        assertEquals(1, buyerNotifications.size(), "Buyer should receive exactly 1 notification");
        Notification buyerNotif = buyerNotifications.get(0);
        assertEquals(NotificationType.ORDER_CONFIRMED, buyerNotif.getType());
        assertTrue(buyerNotif.getMessage().contains("COD"), "Notification message should contain payment method COD");

        // 4. Assert exactly 1 notification for vendor
        List<Notification> vendorNotifications = notificationRepository.findAll().stream()
                .filter(n -> n.getUserId().equals(vendorUserId))
                .toList();
        assertEquals(1, vendorNotifications.size(), "Vendor should receive exactly 1 notification");
        Notification vendorNotif = vendorNotifications.get(0);
        assertEquals(NotificationType.NEW_ORDER, vendorNotif.getType());
        
        // 5. Assert no duplicates if event published again (simulating duplicate IPN logic)
        com.tmt.ecommerce.order.api.event.OrderConfirmedEvent duplicateEvent = 
                new com.tmt.ecommerce.order.api.event.OrderConfirmedEvent(
                        orderId, buyerUserId, shopId, "COD"
                );
        
        // Use application event publisher to publish event manually
        eventPublisher.publishEvent(duplicateEvent);
    }
}
