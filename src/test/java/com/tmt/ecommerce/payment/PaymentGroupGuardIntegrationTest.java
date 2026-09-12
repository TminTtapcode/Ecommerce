package com.tmt.ecommerce.payment;

import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.order.api.event.OrderCancelledEvent;
import com.tmt.ecommerce.order.api.dto.OrderItemCancelDto;
import com.tmt.ecommerce.order.dto.request.VendorOrderStatusUpdateRequest;
import com.tmt.ecommerce.order.entity.*;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.order.service.OrderService;
import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.product.entity.*;
import com.tmt.ecommerce.product.enums.ProductStatus;
import com.tmt.ecommerce.product.repository.*;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentGroupGuardIntegrationTest {
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Autowired com.tmt.ecommerce.identity.repository.UserRepository users;
    @Autowired com.tmt.ecommerce.identity.repository.RoleRepository roles;
    @Autowired com.tmt.ecommerce.common.security.JwtService jwt;
    @Autowired com.tmt.ecommerce.cart.service.CartService cart;
    @Autowired OrderService service;
    @Autowired com.tmt.ecommerce.payment.service.PaymentGatewayService gateway;
    @Autowired com.tmt.ecommerce.payment.repository.PaymentRepository payments;
    @org.springframework.beans.factory.annotation.Value("${payment.vnpay.secret-key}") String secret;

    @Autowired OrderRepository orders;
    @Autowired ProductInternalService inventory;
    @Autowired ProductRepository products;
    @Autowired ProductVariantRepository variants;
    @Autowired CategoryRepository categories;
    @Autowired ShopRepository shops;
    @Autowired PlatformTransactionManager manager;
    @Autowired JdbcTemplate jdbc;
    @Autowired ApplicationEventPublisher events;
    TransactionTemplate tx;
    Long owner, shop, product, v1, v2, order;
    String token;

    @BeforeEach void setup() {

        assertTrue(jdbc.queryForObject("select database()", String.class).startsWith("ecommerce_s103_s104"));
        tx = new TransactionTemplate(manager);
        var vendorRole = roles.findByName("ROLE_VENDOR").orElseGet(() -> roles.save(
                com.tmt.ecommerce.identity.entity.Role.builder().name("ROLE_VENDOR").build()));
        var user = users.save(com.tmt.ecommerce.identity.entity.User.builder()
                .email("s103-" + UUID.randomUUID() + "@example.test").fullName("Test vendor")
                .passwordHash("unused-test-hash").status("ACTIVE").roles(java.util.Set.of(vendorRole)).build());
        owner = user.getId();
        token = jwt.generateToken(new com.tmt.ecommerce.identity.security.CustomUserDetails(user));
        shop = shops.save(Shop.builder().name("s103-" + UUID.randomUUID()).userId(owner)
                .status(ShopStatus.ACTIVE).build()).getId();
        Category category = categories.save(Category.builder().name("s103-" + UUID.randomUUID()).build());
        Product p = products.save(Product.builder().shopId(shop).category(category).name("Cancellation fixture")
                .price(BigDecimal.TEN).stockQuantity(30).status(ProductStatus.ACTIVE).build());
        product = p.getId();
        v1 = variant(p, 10); v2 = variant(p, 20);
        order = newOrder(OrderStatus.CONFIRMED, v1, 2, v2, 3);
    }
    Long variant(Product p, int stock) {
        return variants.save(ProductVariant.builder().product(p).sku("s103-" + UUID.randomUUID())
                .price(BigDecimal.TEN).stockQuantity(stock).status(ProductStatus.ACTIVE).build()).getId();
    }
    Long newOrder(OrderStatus state, Long first, int q1, Long second, int q2) {
        Order o = Order.builder().userId(owner).shopId(shop).status(state).paymentMethod("COD")
                .paymentGroupId("s103-" + UUID.randomUUID()).totalAmount(BigDecimal.TEN).shippingAddress("Test").build();
        for (var item : List.of(new OrderItemCancelDto(first, q1), new OrderItemCancelDto(second, q2))) {
            o.addOrderItem(OrderItem.builder().productVariantId(item.variantId()).quantity(item.quantity())
                    .productName("Test").unitPrice(BigDecimal.TEN).subTotal(BigDecimal.TEN).build());
        }
        return orders.save(o).getId();
    }
    void transition(Long id, OrderStatus status) {
        service.updateVendorOrderStatus(owner, id, new VendorOrderStatusUpdateRequest(status, null));
    }
    void assertState(Long id, OrderStatus status, int s1, int s2, int total) {
        assertEquals(status, orders.findById(id).orElseThrow().getStatus());
        assertEquals(s1, variants.findById(v1).orElseThrow().getStockQuantity());
        assertEquals(s2, variants.findById(v2).orElseThrow().getStockQuantity());
        assertEquals(total, products.findById(product).orElseThrow().getStockQuantity());
    }
    com.tmt.ecommerce.payment.entity.Payment attempt(String group) {
        gateway.generatePaymentUrl(new com.tmt.ecommerce.payment.dto.request.PaymentOrderData(
                group, BigDecimal.TEN, "Test", com.tmt.ecommerce.payment.enums.PaymentMethod.VNPAY), "127.0.0.1");
        return payments.findByPaymentGroupIdOrderByCreatedAtDesc(group).getFirst();
    }
    String group() { return orders.findById(order).orElseThrow().getPaymentGroupId(); }
    java.util.Map<String, String> callback(com.tmt.ecommerce.payment.entity.Payment payment, String code) {
        var params = new java.util.TreeMap<String, String>();
        params.put("vnp_TxnRef", payment.getTransactionId());
        params.put("vnp_Amount", "1000");
        params.put("vnp_ResponseCode", code);
        params.put("vnp_TransactionNo", "test-" + payment.getId());
        String data = params.entrySet().stream().map(e -> e.getKey() + "=" +
                java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.US_ASCII))
                .collect(java.util.stream.Collectors.joining("&"));
        params.put("vnp_SecureHash", com.tmt.ecommerce.payment.util.HashUtil.hmacSha512(secret, data));
        return params;
    }
    void ipn(com.tmt.ecommerce.payment.entity.Payment p) { gateway.processVnPayIpn(callback(p, "00")); }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.NullAndEmptySource
    @org.junit.jupiter.params.provider.ValueSource(strings = {"999", "abc", "-1000", "99999999999999999999999"})
    void signedMissingOrInvalidAmountDoesNotChangePaymentOrOrder(String amount) {
        var p = attempt(group()); var params = callback(p, "00");
        params.remove("vnp_SecureHash");
        if (amount == null) params.remove("vnp_Amount"); else params.put("vnp_Amount", amount);
        resign(params);
        assertThrows(IllegalArgumentException.class, () -> gateway.processVnPayIpn(params));
        assertEquals(com.tmt.ecommerce.payment.enums.PaymentStatus.PENDING, payments.findById(p.getId()).orElseThrow().getStatus());
        assertState(order, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void fractionalStoredAmountIsComparedWithoutTruncation() {
        var p = attempt(group());
        tx.executeWithoutResult(s -> payments.findById(p.getId()).orElseThrow().setAmount(new BigDecimal("10.50")));
        var params = callback(p, "00"); params.put("vnp_Amount", "1050"); params.remove("vnp_SecureHash"); resign(params);
        gateway.processVnPayIpn(params); assertPaid(p);
    }
    void resign(java.util.Map<String, String> params) {
        String data = params.entrySet().stream().filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.US_ASCII))
                .collect(java.util.stream.Collectors.joining("&"));
        params.put("vnp_SecureHash", com.tmt.ecommerce.payment.util.HashUtil.hmacSha512(secret, data));
    }
    void assertPaid(com.tmt.ecommerce.payment.entity.Payment p) {
        var saved = payments.findById(p.getId()).orElseThrow();
        assertEquals(com.tmt.ecommerce.payment.enums.PaymentStatus.SUCCESS, saved.getStatus());
        assertNotNull(saved.getGatewayTransactionId()); assertNotNull(saved.getRawData());
    }
    @Test void lateIpnAfterCancellationCannotRestoreOrResurrect() {
        var p = attempt(group());
        transition(order, OrderStatus.CANCELLED);
        ipn(p); ipn(p);
        assertPaid(p);
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
        assertThrows(AppException.class, () -> transition(order, OrderStatus.CANCELLED));
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"CONFIRMED", "SHIPPED", "DELIVERED"})
    void preservesExistingState(OrderStatus state) {
        tx.executeWithoutResult(s -> orders.findById(order).orElseThrow().setStatus(state));
        var p = attempt(group()); ipn(p); assertPaid(p);
        assertState(order, state, 10, 20, 30);
    }
    @Test void mixedGroupConfirmsOnlyPendingAndDuplicateDoesNotPublishAgain() {
        String group = group();
        var ids = new java.util.ArrayList<Long>();
        for (OrderStatus status : OrderStatus.values()) {
            Long id = newOrder(status, v1, 2, v2, 3); ids.add(id);
            tx.executeWithoutResult(s -> orders.findById(id).orElseThrow().setPaymentGroupId(group));
        }
        var p = attempt(group()); ipn(p); ipn(p);
        for (int i = 0; i < ids.size(); i++) {
            OrderStatus original = OrderStatus.values()[i];
            assertEquals(original == OrderStatus.PENDING ? OrderStatus.CONFIRMED : original,
                    orders.findById(ids.get(i)).orElseThrow().getStatus());
            assertEquals(original == OrderStatus.PENDING ? 1 : 0, probe.count(ids.get(i)));
        }
        assertPaid(p); assertState(order, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void failedIpnLeavesOrderPending() {
        tx.executeWithoutResult(s -> orders.findById(order).orElseThrow().setStatus(OrderStatus.PENDING));
        var p = attempt(group()); gateway.processVnPayIpn(callback(p, "24")); ipn(p);
        assertEquals(com.tmt.ecommerce.payment.enums.PaymentStatus.FAILED, payments.findById(p.getId()).orElseThrow().getStatus());
        assertState(order, OrderStatus.PENDING, 10, 20, 30); assertEquals(0, probe.count(order));
    }
    @Test void missingOrderGroupStillRecordsSuccessfulPayment() {
        var p = attempt("missing-" + UUID.randomUUID());
        ipn(p); assertPaid(p);
        assertState(order, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void callerRollbackRevertsPaymentAndOrderAndSuppressesEvent() {
        tx.executeWithoutResult(s -> orders.findById(order).orElseThrow().setStatus(OrderStatus.PENDING));
        var p = attempt(group());
        tx.executeWithoutResult(s -> { ipn(p); s.setRollbackOnly(); });
        assertEquals(com.tmt.ecommerce.payment.enums.PaymentStatus.PENDING, payments.findById(p.getId()).orElseThrow().getStatus());
        assertState(order, OrderStatus.PENDING, 10, 20, 30); assertEquals(0, probe.count(order));
    }
    @Test void cancellationCommitsWhileIpnWaitsOnOrder() throws Exception {
        var p = attempt(group());
        serialRace(() -> {
            orders.findByIdForUpdate(order).orElseThrow();
        }, () -> transition(order, OrderStatus.CANCELLED), () -> ipn(p));
        assertPaid(p); assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @Test void ipnCommitsWhileCancellationWaitsOnOrder() throws Exception {
        var p = attempt(group());
        serialRace(() -> ipn(p), () -> {}, () -> transition(order, OrderStatus.CANCELLED));
        assertPaid(p); assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @Test void simultaneousDuplicateIpnHasOneConfirmation() throws Exception {
        tx.executeWithoutResult(s -> orders.findById(order).orElseThrow().setStatus(OrderStatus.PENDING));
        var p = attempt(group());
        serialRace(() -> payments.findByTransactionIdForUpdate(p.getTransactionId()).orElseThrow(),
                () -> ipn(p), () -> ipn(p));
        assertPaid(p); assertState(order, OrderStatus.CONFIRMED, 10, 20, 30);
        assertEquals(1, probe.count(order));
    }
    @Test void differentAttemptsSameGroupHaveOneConfirmation() throws Exception {
        tx.executeWithoutResult(s -> orders.findById(order).orElseThrow().setStatus(OrderStatus.PENDING));
        var first = attempt(group()); var second = attempt(group());
        serialRace(() -> ipn(first), () -> {}, () -> ipn(second));
        assertPaid(first); assertPaid(second);
        assertState(order, OrderStatus.CONFIRMED, 10, 20, 30); assertEquals(1, probe.count(order));
    }
    @Test void signedHttpIpnKeepsGatewayEnvelopeForLateAndDuplicateCallbacks() throws Exception {
        var p = attempt(group()); transition(order, OrderStatus.CANCELLED);
        String query = callback(p, "00").entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(java.util.stream.Collectors.joining("&"));
        var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                "http://127.0.0.1:" + port + "/api/v1/payments/vnpay/ipn?" + query)).GET().build();
        for (int i = 0; i < 2; i++) {
            var response = java.net.http.HttpClient.newHttpClient().send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            var body = tools.jackson.databind.json.JsonMapper.builder().build().readTree(response.body());
            assertEquals("00", body.get("RspCode").asText()); assertFalse(body.has("success"));
        }
        assertPaid(p); assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    void serialRace(Runnable lock, Runnable finish, Runnable competitor) throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var locked = new CountDownLatch(1); var release = new CountDownLatch(1); var started = new CountDownLatch(1);
        try {
            Future<?> first = pool.submit(() -> tx.executeWithoutResult(s -> {
                lock.run(); locked.countDown(); await(release); finish.run();
            }));
            assertTrue(locked.await(10, TimeUnit.SECONDS));
            Future<?> second = pool.submit(() -> { started.countDown(); competitor.run(); });
            assertTrue(started.await(10, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> second.get(300, TimeUnit.MILLISECONDS));
            release.countDown(); first.get(15, TimeUnit.SECONDS); second.get(15, TimeUnit.SECONDS);
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS)); }
    }
    static void await(CountDownLatch latch) {
        try { if (!latch.await(15, TimeUnit.SECONDS)) throw new AssertionError("Latch timeout"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }
    @Autowired ConfirmationProbe probe;
    @org.springframework.boot.test.context.TestConfiguration
    static class ProbeConfig {
        @org.springframework.context.annotation.Bean ConfirmationProbe confirmationProbe() { return new ConfirmationProbe(); }
    }
    static class ConfirmationProbe {
        private final java.util.concurrent.ConcurrentMap<Long, Integer> counts = new ConcurrentHashMap<>();
        @org.springframework.transaction.event.TransactionalEventListener
        public void confirmed(com.tmt.ecommerce.order.api.event.OrderConfirmedEvent e) {
            counts.merge(e.orderId(), 1, Integer::sum);
        }
        int count(Long id) { return counts.getOrDefault(id, 0); }
    }
}
