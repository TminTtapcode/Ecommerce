package com.tmt.ecommerce;

import com.tmt.ecommerce.cart.entity.Cart;
import com.tmt.ecommerce.cart.entity.CartItem;
import com.tmt.ecommerce.cart.repository.CartRepository;
import com.tmt.ecommerce.identity.entity.Role;
import com.tmt.ecommerce.identity.entity.User;
import com.tmt.ecommerce.identity.repository.RoleRepository;
import com.tmt.ecommerce.identity.repository.UserRepository;
import com.tmt.ecommerce.notification.entity.Notification;
import com.tmt.ecommerce.notification.entity.NotificationType;
import com.tmt.ecommerce.notification.repository.NotificationRepository;
import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderItem;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.order.service.OrderService;
import com.tmt.ecommerce.payment.entity.Payment;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.enums.PaymentStatus;
import com.tmt.ecommerce.payment.factory.PaymentStrategyFactory;
import com.tmt.ecommerce.payment.repository.PaymentRepository;
import com.tmt.ecommerce.payment.service.PaymentGatewayService;
import com.tmt.ecommerce.payment.strategy.PaymentStrategy;
import com.tmt.ecommerce.product.entity.Category;
import com.tmt.ecommerce.product.entity.Product;
import com.tmt.ecommerce.product.entity.ProductVariant;
import com.tmt.ecommerce.product.enums.ProductStatus;
import com.tmt.ecommerce.product.repository.CategoryRepository;
import com.tmt.ecommerce.product.repository.ProductRepository;
import com.tmt.ecommerce.product.repository.ProductVariantRepository;
import com.tmt.ecommerce.review.api.event.ReviewCreatedEvent;
import com.tmt.ecommerce.review.dto.request.ReviewCreateRequest;
import com.tmt.ecommerce.review.entity.Review;
import com.tmt.ecommerce.review.repository.ReviewRepository;
import com.tmt.ecommerce.review.service.ReviewService;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import com.tmt.ecommerce.voucher.entity.Voucher;
import com.tmt.ecommerce.voucher.entity.VoucherScope;
import com.tmt.ecommerce.voucher.entity.VoucherType;
import com.tmt.ecommerce.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TransactionAuditIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentStrategyFactory paymentStrategyFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean(name = "vnPayStrategy")
    private PaymentStrategy mockVnPayStrategy;

    private Role roleUser;
    private Role roleVendor;
    private Category testCategory;

    @BeforeEach
    void setUp() {

        notificationRepository.deleteAll();
        reviewRepository.deleteAll();
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        voucherRepository.deleteAll();
        productVariantRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        shopRepository.deleteAll();

        roleUser = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            return roleRepository.save(r);
        });

        roleVendor = roleRepository.findByName("ROLE_VENDOR").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_VENDOR");
            return roleRepository.save(r);
        });

        testCategory = categoryRepository.save(
                Category.builder()
                        .name("Test Category " + System.currentTimeMillis())
                        .build()
        );

        when(paymentStrategyFactory.getStrategy(PaymentMethod.VNPAY)).thenReturn(mockVnPayStrategy);
        when(mockVnPayStrategy.getPaymentMethod()).thenReturn(PaymentMethod.VNPAY);
        when(mockVnPayStrategy.verifyIpnSignature(any())).thenReturn(true);
    }

    private User createTestUser(String emailPrefix, Role role) {
        String unique = emailPrefix + "_" + System.nanoTime();
        User user = User.builder()
                .email(unique + "@example.com")
                .passwordHash("hashed_pw")
                .fullName("User " + unique)
                .status("ACTIVE")
                .roles(Set.of(role))
                .build();
        return userRepository.save(user);
    }

    private Shop createTestShop(User owner, String shopName) {
        Shop shop = Shop.builder()
                .userId(owner.getId())
                .name(shopName + "_" + System.nanoTime())
                .description("Test shop description")
                .status(ShopStatus.ACTIVE)
                .build();
        return shopRepository.save(shop);
    }

    private Product createTestProduct(Shop shop, String name) {
        Product product = Product.builder()
                .shopId(shop.getId())
                .category(testCategory)
                .name(name + "_" + System.nanoTime())
                .description("Product description")
                .price(new BigDecimal("100000"))
                .stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        return productRepository.save(product);
    }

    private ProductVariant createTestVariant(Product product, String sku, int stock, BigDecimal price) {
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(sku + "_" + System.nanoTime())
                .stockQuantity(stock)
                .price(price)
                .attributes(Map.of("color", "black"))
                .status(ProductStatus.ACTIVE)
                .build();
        return productVariantRepository.save(variant);
    }

    @Test
    @DisplayName("Multi-Shop Checkout: Atomicity & Rollback when Shop B has insufficient stock")
    void testMultiShopCheckout_RollbackWhenShopBInsufficientStock() {

        User buyer = createTestUser("buyer_checkout", roleUser);
        User vendorA = createTestUser("vendor_a", roleVendor);
        User vendorB = createTestUser("vendor_b", roleVendor);

        Shop shopA = createTestShop(vendorA, "Shop A");
        Shop shopB = createTestShop(vendorB, "Shop B");

        Product productA = createTestProduct(shopA, "Product A");
        ProductVariant variantA = createTestVariant(productA, "SKU-A", 10, new BigDecimal("100000"));

        Product productB = createTestProduct(shopB, "Product B");
        ProductVariant variantB = createTestVariant(productB, "SKU-B", 1, new BigDecimal("100000"));

        String voucherCode = "VOUCHER_TX_" + System.currentTimeMillis();
        Voucher voucher = Voucher.builder()
                .code(voucherCode)
                .scope(VoucherScope.SYSTEM)
                .type(VoucherType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("20000"))
                .minOrderValue(new BigDecimal("50000"))
                .usageLimit(100)
                .usedCount(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .build();
        voucher = voucherRepository.save(voucher);

        Cart cart = Cart.builder().userId(buyer.getId()).build();
        cart = cartRepository.save(cart);

        CartItem itemA = CartItem.builder()
                .cart(cart)
                .productVariantId(variantA.getId())
                .quantity(2)
                .build();

        CartItem itemB = CartItem.builder()
                .cart(cart)
                .productVariantId(variantB.getId())
                .quantity(5)
                .build();

        cart.getItems().add(itemA);
        cart.getItems().add(itemB);
        cartRepository.save(cart);

        CheckoutRequest request = new CheckoutRequest(
                "789 Delivery Rd, City",
                "COD",
                voucherCode,
                null
        );

        Long buyerId = buyer.getId();
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            orderService.checkout(buyerId, request);
        }, "Checkout must throw IllegalStateException when any product has insufficient stock");

        assertTrue(ex.getMessage().contains("hết hàng hoặc không đủ số lượng"),
                "Exception message should indicate out of stock");

        List<Order> ordersInDb = orderRepository.findAll();
        assertEquals(0, ordersInDb.size(),
                "No orders should be persisted when multi-shop checkout fails");

        ProductVariant refreshedVariantA = productVariantRepository.findById(variantA.getId()).orElseThrow();
        assertEquals(10, refreshedVariantA.getStockQuantity(),
                "Stock of Shop A variant must be rolled back to initial value (10)");

        ProductVariant refreshedVariantB = productVariantRepository.findById(variantB.getId()).orElseThrow();
        assertEquals(1, refreshedVariantB.getStockQuantity(),
                "Stock of Shop B variant must remain 1");

        Voucher refreshedVoucher = voucherRepository.findById(voucher.getId()).orElseThrow();
        assertEquals(0, refreshedVoucher.getUsedCount(),
                "Voucher usedCount must remain 0");

        Cart refreshedCart = cartRepository.findByUserId(buyer.getId()).orElseThrow();
        assertEquals(2, refreshedCart.getItems().size(),
                "Cart items must not be cleared when checkout transaction rolls back");
    }

    @Test
    @DisplayName("Review Notification: AFTER_COMMIT creates notification in new transaction; Rollback does not")
    void testReviewNotification_AfterCommitAndRollback() {
        User buyer = createTestUser("buyer_review", roleUser);
        User vendor = createTestUser("vendor_review", roleVendor);
        Shop shop = createTestShop(vendor, "Shop Review");
        Product product = createTestProduct(shop, "Reviewable Product");
        ProductVariant variant = createTestVariant(product, "SKU-REV", 20, new BigDecimal("150000"));

        Order deliveredOrder = Order.builder()
                .userId(buyer.getId())
                .shopId(shop.getId())
                .status(OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("150000"))
                .originalTotalAmount(new BigDecimal("150000"))
                .discountAmount(BigDecimal.ZERO)
                .shippingAddress("456 Test Street")
                .paymentMethod("COD")
                .paymentGroupId("PG_REV_" + System.currentTimeMillis())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(deliveredOrder)
                .productVariantId(variant.getId())
                .productName(product.getName())
                .quantity(1)
                .unitPrice(variant.getPrice())
                .subTotal(variant.getPrice())
                .build();

        deliveredOrder.getItems().add(orderItem);
        deliveredOrder = orderRepository.save(deliveredOrder);

        ReviewCreateRequest reviewReq = new ReviewCreateRequest(
                product.getId(),
                deliveredOrder.getId(),
                5,
                "Tuyet voi, san pham rat chat luong!",
                null
        );

        reviewService.createReview(buyer.getId(), reviewReq);

        List<Review> reviews = reviewRepository.findAll();
        assertEquals(1, reviews.size());
        assertEquals(buyer.getId(), reviews.get(0).getUserId());

        List<Notification> notifications = notificationRepository.findAll();
        assertEquals(1, notifications.size(), "Vendor should receive 1 notification for new review");
        Notification vendorNotif = notifications.get(0);
        assertEquals(vendor.getId(), vendorNotif.getUserId());
        assertEquals(NotificationType.NEW_REVIEW, vendorNotif.getType());
        assertEquals(String.valueOf(reviews.get(0).getId()), vendorNotif.getReferenceId());

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        assertThrows(RuntimeException.class, () -> {
            txTemplate.execute(status -> {

                eventPublisher.publishEvent(new ReviewCreatedEvent(
                        99999L, buyer.getId(), shop.getId(), product.getId(), 5
                ));

                throw new RuntimeException("Forced rollback in review transaction");
            });
        });

        List<Notification> postRollbackNotifs = notificationRepository.findAll();
        assertEquals(1, postRollbackNotifs.size(),
                "Notification count must not increase because AFTER_COMMIT listener only triggers on successful commit");
    }

    @Test
    @DisplayName("VNPay IPN Transaction: Success commits both Payment and Orders; Exception rolls back both; Idempotency prevents duplicates")
    void testVnPayIpn_TransactionAtomicityAndIdempotency() {
        User buyer = createTestUser("buyer_vnpay", roleUser);
        User vendor1 = createTestUser("vendor_vnp1", roleVendor);
        User vendor2 = createTestUser("vendor_vnp2", roleVendor);
        Shop shop1 = createTestShop(vendor1, "Shop 1");
        Shop shop2 = createTestShop(vendor2, "Shop 2");

        String paymentGroupId = "PG_VNP_" + System.currentTimeMillis();

        Order order1 = Order.builder()
                .userId(buyer.getId())
                .shopId(shop1.getId())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100000"))
                .originalTotalAmount(new BigDecimal("100000"))
                .discountAmount(BigDecimal.ZERO)
                .shippingAddress("123 Street")
                .paymentMethod("VNPAY")
                .paymentGroupId(paymentGroupId)
                .build();

        Order order2 = Order.builder()
                .userId(buyer.getId())
                .shopId(shop2.getId())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("150000"))
                .originalTotalAmount(new BigDecimal("150000"))
                .discountAmount(BigDecimal.ZERO)
                .shippingAddress("123 Street")
                .paymentMethod("VNPAY")
                .paymentGroupId(paymentGroupId)
                .build();

        orderRepository.save(order1);
        orderRepository.save(order2);

        String txnRef = "TXN_" + System.currentTimeMillis();
        Payment payment = Payment.builder()
                .paymentGroupId(paymentGroupId)
                .amount(new BigDecimal("250000"))
                .method(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .transactionId(txnRef)
                .build();
        paymentRepository.save(payment);

        Map<String, String> invalidParams = new HashMap<>();
        invalidParams.put("vnp_TxnRef", txnRef);
        invalidParams.put("vnp_ResponseCode", "00");
        invalidParams.put("vnp_TransactionNo", "GW_INVALID");
        invalidParams.put("vnp_Amount", "99999900");

        assertThrows(IllegalArgumentException.class, () -> {
            paymentGatewayService.processVnPayIpn(invalidParams);
        }, "IPN should throw IllegalArgumentException when amount does not match");

        Payment paymentAfterFailedIpn = paymentRepository.findByTransactionId(txnRef).orElseThrow();
        assertEquals(PaymentStatus.PENDING, paymentAfterFailedIpn.getStatus(),
                "Payment must remain PENDING after failed IPN transaction");

        List<Order> ordersAfterFailedIpn = orderRepository.findByPaymentGroupId(paymentGroupId);
        for (Order o : ordersAfterFailedIpn) {
            assertEquals(OrderStatus.PENDING, o.getStatus(),
                    "Orders must remain PENDING after failed IPN transaction");
        }

        Map<String, String> validParams = new HashMap<>();
        validParams.put("vnp_TxnRef", txnRef);
        validParams.put("vnp_ResponseCode", "00");
        validParams.put("vnp_TransactionNo", "GW_SUCCESS_12345");
        validParams.put("vnp_Amount", "25000000");

        paymentGatewayService.processVnPayIpn(validParams);

        Payment paymentAfterSuccess = paymentRepository.findByTransactionId(txnRef).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, paymentAfterSuccess.getStatus(),
                "Payment status must be SUCCESS");
        assertEquals("GW_SUCCESS_12345", paymentAfterSuccess.getGatewayTransactionId());

        List<Order> ordersAfterSuccess = orderRepository.findByPaymentGroupId(paymentGroupId);
        assertEquals(2, ordersAfterSuccess.size());
        for (Order o : ordersAfterSuccess) {
            assertEquals(OrderStatus.CONFIRMED, o.getStatus(),
                    "Order in payment group must transition to CONFIRMED");
        }

        paymentGatewayService.processVnPayIpn(validParams);

        Payment paymentAfterDuplicate = paymentRepository.findByTransactionId(txnRef).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, paymentAfterDuplicate.getStatus(),
                "Duplicate IPN must keep Payment status as SUCCESS");

        List<Order> ordersAfterDuplicate = orderRepository.findByPaymentGroupId(paymentGroupId);
        for (Order o : ordersAfterDuplicate) {
            assertEquals(OrderStatus.CONFIRMED, o.getStatus(),
                    "Duplicate IPN must keep Order status as CONFIRMED");
        }
    }
}
