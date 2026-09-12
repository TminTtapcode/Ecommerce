package com.tmt.ecommerce.order;

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
class AdminOrderIntegrationTest {
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Autowired com.tmt.ecommerce.identity.repository.UserRepository users;
    @Autowired com.tmt.ecommerce.identity.repository.RoleRepository roles;
    @Autowired com.tmt.ecommerce.common.security.JwtService jwt;
    @Autowired com.tmt.ecommerce.cart.service.CartService cart;
    @Autowired OrderService service;
    @Autowired OrderRepository orders;
    @Autowired ProductInternalService inventory;
    @Autowired ProductRepository products;
    @Autowired ProductVariantRepository variants;
    @Autowired CategoryRepository categories;
    @Autowired ShopRepository shops;
    @Autowired PlatformTransactionManager manager;
    @Autowired JdbcTemplate jdbc;
    @Autowired ApplicationEventPublisher events;
    @Autowired com.tmt.ecommerce.order.service.AdminOrderService adminService;
    @Autowired com.tmt.ecommerce.order.repository.OrderAdminStatusHistoryRepository history;
    @Autowired com.tmt.ecommerce.payment.repository.PaymentRepository payments;
    @Autowired com.tmt.ecommerce.payment.service.PaymentGatewayService gateway;
    @org.springframework.beans.factory.annotation.Value("${payment.vnpay.secret-key}") String secret;
    com.tmt.ecommerce.identity.security.CustomUserDetails adminPrincipal;
    String adminToken;
    TransactionTemplate tx;
    Long owner, shop, product, v1, v2, order;
    String token;

    @BeforeEach void setup() {

        assertTrue(jdbc.queryForObject("select database()", String.class).startsWith("ecommerce_s103_s104_s105"));
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
        var role = roles.findByName("ROLE_ADMIN").orElseGet(() -> roles.save(
                com.tmt.ecommerce.identity.entity.Role.builder().name("ROLE_ADMIN").build()));
        var admin = users.save(com.tmt.ecommerce.identity.entity.User.builder()
                .email("admin-" + UUID.randomUUID() + "@example.test").fullName("Admin")
                .passwordHash("unused").status("ACTIVE").roles(java.util.Set.of(role)).build());
        adminPrincipal = new com.tmt.ecommerce.identity.security.CustomUserDetails(admin);
        adminToken = jwt.generateToken(adminPrincipal);
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
    <T> T asAdmin(java.util.concurrent.Callable<T> action) {
        var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
        var before = context.getAuthentication();
        context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                adminPrincipal, null, adminPrincipal.getAuthorities()));
        try { return action.call(); }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
        finally { context.setAuthentication(before); }
    }
    void admin(OrderStatus expected, OrderStatus target) {
        asAdmin(() -> adminService.update(order, new com.tmt.ecommerce.order.dto.request.AdminOrderStatusRequest(expected, target, "  Audit reason  ")));
    }
    long auditCount() { return history.findByOrderIdOrderByIdDesc(order, org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements(); }
    void state(OrderStatus state, String method) {
        tx.executeWithoutResult(s -> { var o = orders.findById(order).orElseThrow(); o.setStatus(state); o.setPaymentMethod(method); });
    }
    String group() { return orders.findById(order).orElseThrow().getPaymentGroupId(); }
    com.tmt.ecommerce.payment.entity.Payment payment(String group, com.tmt.ecommerce.payment.enums.PaymentMethod method,
            com.tmt.ecommerce.payment.enums.PaymentStatus status) {
        return payments.save(com.tmt.ecommerce.payment.entity.Payment.builder().paymentGroupId(group).amount(BigDecimal.TEN)
                .method(method).status(status).transactionId("p101-" + UUID.randomUUID()).build());
    }
    @Test void codCancellationAcrossShopAndBanRecordsPrincipalAndRestoresOnce() {
        tx.executeWithoutResult(s -> shops.findById(shop).orElseThrow().setStatus(ShopStatus.BANNED));
        admin(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
        var entries = history.findByOrderIdOrderByIdDesc(order, org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(1, entries.getTotalElements());
        var entry = entries.getContent().getFirst();
        assertEquals(adminPrincipal.getId(), entry.getActorUserId());
        assertEquals("Audit reason", entry.getReason()); assertNotNull(entry.getCreatedAt());
        assertEquals(ErrorCode.ORDER_STATE_CONFLICT, assertThrows(AppException.class,
                () -> admin(OrderStatus.CONFIRMED, OrderStatus.CANCELLED)).getErrorCode());
        assertEquals(1, auditCount()); assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PENDING", "DELIVERED", "CANCELLED"})
    void terminalAndPendingHaveNoActions(OrderStatus source) {
        state(source, "COD");
        assertTrue(asAdmin(() -> adminService.allowedTransitions(order)).isEmpty());
        assertEquals(ErrorCode.INVALID_ORDER_STATE_TRANSITION, assertThrows(AppException.class,
                () -> admin(source, OrderStatus.SHIPPED)).getErrorCode());
        assertEquals(0, auditCount());
    }
    @ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"CONFIRMED,CONFIRMED", "CONFIRMED,DELIVERED", "SHIPPED,CONFIRMED", "SHIPPED,CANCELLED", "SHIPPED,SHIPPED"})
    void rejectsOtherTransitions(OrderStatus source, OrderStatus target) {
        state(source, "COD");
        assertEquals(ErrorCode.INVALID_ORDER_STATE_TRANSITION, assertThrows(AppException.class, () -> admin(source, target)).getErrorCode());
        assertEquals(0, auditCount());
    }
    @Test void codShippingAndDeliveryKeepStockAndHistoryOrdered() {
        admin(OrderStatus.CONFIRMED, OrderStatus.SHIPPED); admin(OrderStatus.SHIPPED, OrderStatus.DELIVERED);
        assertState(order, OrderStatus.DELIVERED, 10, 20, 30);
        var page = asAdmin(() -> adminService.history(order, 0, 1));
        assertEquals(2, page.getTotalElements()); assertEquals("DELIVERED", page.getContent().getFirst().newStatus());
        assertEquals("SHIPPED", asAdmin(() -> adminService.history(order, 1, 1)).getContent().getFirst().newStatus());
    }
    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"NONE", "PENDING", "FAILED", "OTHER_GROUP", "OTHER_METHOD", "UNKNOWN_METHOD"})
    void requiresMatchingCommittedVnpaySuccess(String scenario) {
        state(OrderStatus.CONFIRMED, scenario.equals("UNKNOWN_METHOD") ? "UNKNOWN" : "VNPAY");
        if (!scenario.equals("NONE") && !scenario.equals("UNKNOWN_METHOD")) {
            payment(scenario.equals("OTHER_GROUP") ? "other-" + UUID.randomUUID() : group(),
                    scenario.equals("OTHER_METHOD") ? com.tmt.ecommerce.payment.enums.PaymentMethod.COD : com.tmt.ecommerce.payment.enums.PaymentMethod.VNPAY,
                    scenario.equals("PENDING") ? com.tmt.ecommerce.payment.enums.PaymentStatus.PENDING : scenario.equals("FAILED")
                            ? com.tmt.ecommerce.payment.enums.PaymentStatus.FAILED : com.tmt.ecommerce.payment.enums.PaymentStatus.SUCCESS);
        }
        assertTrue(asAdmin(() -> adminService.allowedTransitions(order)).isEmpty());
        assertEquals(ErrorCode.ORDER_PAYMENT_CONDITION_NOT_MET, assertThrows(AppException.class,
                () -> admin(OrderStatus.CONFIRMED, OrderStatus.SHIPPED)).getErrorCode());
        assertEquals(0, auditCount());
    }
    @Test void successSurvivesNewerAttemptsAndNeverAllowsVnpayCancellation() {
        state(OrderStatus.CONFIRMED, "VNPAY");
        for (var status : List.of(com.tmt.ecommerce.payment.enums.PaymentStatus.SUCCESS, com.tmt.ecommerce.payment.enums.PaymentStatus.SUCCESS,
                com.tmt.ecommerce.payment.enums.PaymentStatus.FAILED, com.tmt.ecommerce.payment.enums.PaymentStatus.PENDING))
            payment(group(), com.tmt.ecommerce.payment.enums.PaymentMethod.VNPAY, status);
        assertEquals(List.of(OrderStatus.SHIPPED), asAdmin(() -> adminService.allowedTransitions(order)));
        assertEquals(ErrorCode.ORDER_PAYMENT_CONDITION_NOT_MET, assertThrows(AppException.class,
                () -> admin(OrderStatus.CONFIRMED, OrderStatus.CANCELLED)).getErrorCode());
        admin(OrderStatus.CONFIRMED, OrderStatus.SHIPPED); admin(OrderStatus.SHIPPED, OrderStatus.DELIVERED);
        assertState(order, OrderStatus.DELIVERED, 10, 20, 30); assertEquals(2, auditCount());
    }
    @Test void restoreFailureRollsBackStatusStockAndHistory() {
        tx.executeWithoutResult(s -> orders.findById(order).orElseThrow().getItems().get(1).setProductVariantId(Long.MAX_VALUE));
        assertThrows(AppException.class, () -> admin(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        assertState(order, OrderStatus.CONFIRMED, 10, 20, 30); assertEquals(0, auditCount());
    }
    @Test void callerRollbackRollsBackHistoryAndStock() {
        tx.executeWithoutResult(s -> { admin(OrderStatus.CONFIRMED, OrderStatus.CANCELLED); s.setRollbackOnly(); });
        assertState(order, OrderStatus.CONFIRMED, 10, 20, 30); assertEquals(0, auditCount());
    }
    @Test void databaseHistoryInsertFailureRollsBackRestoredStock() {

        jdbc.execute("ALTER TABLE order_admin_status_history ADD CONSTRAINT p101_history_failure CHECK (order_id <> " + order + ")");
        try {
            assertThrows(RuntimeException.class, () -> admin(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
            assertState(order, OrderStatus.CONFIRMED, 10, 20, 30); assertEquals(0, auditCount());
        } finally { jdbc.execute("ALTER TABLE order_admin_status_history DROP CHECK p101_history_failure"); }
    }
    @Test void serviceRequiresAdminEvenWithoutHttp() {
        assertThrows(org.springframework.security.core.AuthenticationException.class, () -> adminService.allowedTransitions(order));
        var user = new com.tmt.ecommerce.identity.security.CustomUserDetails(users.findById(owner).orElseThrow());
        var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
        context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        try { assertThrows(AccessDeniedException.class, () -> adminService.update(order,
                new com.tmt.ecommerce.order.dto.request.AdminOrderStatusRequest(OrderStatus.CONFIRMED, OrderStatus.CANCELLED, "test"))); }
        finally { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
    }
    java.net.http.HttpResponse<String> http(String method, String suffix, String bearer, String body) throws Exception {
        var builder = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:" + port + "/api/v1/admin/orders/" + suffix))
                .header("Content-Type", "application/json");
        if (bearer != null) builder.header("Authorization", "Bearer " + bearer);
        return java.net.http.HttpClient.newHttpClient().send(builder.method(method, body == null
                ? java.net.http.HttpRequest.BodyPublishers.noBody() : java.net.http.HttpRequest.BodyPublishers.ofString(body)).build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
    }
    String body(String reason) {
        return "{\"expectedStatus\":\"CONFIRMED\",\"status\":\"CANCELLED\",\"reason\":\"" + reason + "\",\"actorId\":999999}";
    }
    void error(java.net.http.HttpResponse<String> response, int status, String code) {
        assertEquals(status, response.statusCode(), response.body());
        var json = tools.jackson.databind.json.JsonMapper.builder().build().readTree(response.body());
        assertEquals(code, json.get("errorCode").asText()); assertFalse(json.get("success").asBoolean()); assertTrue(json.get("data").isNull());
    }
    @Test void httpCommandAndHistoryEnforceRolesConflictAndActor() throws Exception {
        error(http("PATCH", order + "/status", null, body("test")), 401, "UNAUTHORIZED");
        error(http("PATCH", order + "/status", token, body("test")), 403, "ACCESS_DENIED");
        error(http("GET", order + "/status-history", token, null), 403, "ACCESS_DENIED");
        error(http("GET", order + "/allowed-transitions", token, null), 403, "ACCESS_DENIED");
        error(http("PATCH", Long.MAX_VALUE + "/status", adminToken, body("test")), 404, "ORDER_NOT_FOUND");
        assertEquals(200, http("PATCH", order + "/status", adminToken, body("  HTTP reason  ")).statusCode());
        error(http("PATCH", order + "/status", adminToken, body("test")), 409, "ORDER_STATE_CONFLICT");
        var audit = http("GET", order + "/status-history", adminToken, null);
        assertEquals(200, audit.statusCode());
        var row = tools.jackson.databind.json.JsonMapper.builder().build().readTree(audit.body()).get("data").get("content").get(0);
        assertEquals(adminPrincipal.getId().longValue(), row.get("actorUserId").asLong());
        assertEquals("HTTP reason", row.get("reason").asText()); assertTrue(row.get("createdAt").asText().endsWith("Z"));
    }
    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"", "   ", "LONG"})
    void httpReasonValidation(String reason) throws Exception {
        error(http("PATCH", order + "/status", adminToken, body(reason.equals("LONG") ? "x".repeat(501) : reason)), 400, "INVALID_INPUT");
        assertEquals(0, auditCount());
    }
    @Test void httpTrimsBeforeLimitAndRejectsMissingExpectedStatus() throws Exception {
        error(http("PATCH", order + "/status", adminToken, "{\"status\":\"CANCELLED\",\"reason\":\"test\"}"), 400, "INVALID_INPUT");
        assertEquals(200, http("PATCH", order + "/status", adminToken, body(" " + "x".repeat(500) + " ")).statusCode());
    }
    @Test void historyRejectsInvalidPageAndLegacyHasNoHistory() {
        assertTrue(asAdmin(() -> adminService.history(order, 0, 10)).isEmpty());
        assertThrows(AppException.class, () -> asAdmin(() -> adminService.history(order, -1, 10)));
        assertThrows(AppException.class, () -> asAdmin(() -> adminService.history(order, 0, 101)));
    }
    @Test void simultaneousAdminsOnlyOneCancellation() throws Exception { race(false, OrderStatus.CANCELLED, OrderStatus.CANCELLED); }
    @Test void vendorShippingWinsAdminCancellation() throws Exception { race(true, OrderStatus.SHIPPED, OrderStatus.CANCELLED); }
    @Test void adminShippingRejectsStaleDeliveryRequest() throws Exception { race(false, OrderStatus.SHIPPED, OrderStatus.DELIVERED); }
    void race(boolean vendorFirst, OrderStatus first, OrderStatus second) throws Exception {
        var pool = Executors.newFixedThreadPool(2); var locked = new CountDownLatch(1); var release = new CountDownLatch(1); var started = new CountDownLatch(1);
        try {
            var winner = pool.submit(() -> tx.executeWithoutResult(s -> {
                orders.findByIdForUpdate(order).orElseThrow(); locked.countDown(); await(release);
                if (vendorFirst) transition(order, first); else admin(OrderStatus.CONFIRMED, first);
            }));
            assertTrue(locked.await(10, TimeUnit.SECONDS));
            var loser = pool.submit(() -> { started.countDown(); assertEquals(ErrorCode.ORDER_STATE_CONFLICT,
                    assertThrows(AppException.class, () -> admin(OrderStatus.CONFIRMED, second)).getErrorCode()); });
            assertTrue(started.await(10, TimeUnit.SECONDS)); assertThrows(TimeoutException.class, () -> loser.get(300, TimeUnit.MILLISECONDS));
            release.countDown(); winner.get(15, TimeUnit.SECONDS); loser.get(15, TimeUnit.SECONDS);
            assertState(order, first, first == OrderStatus.CANCELLED ? 12 : 10, first == OrderStatus.CANCELLED ? 23 : 20, first == OrderStatus.CANCELLED ? 35 : 30);
            assertEquals(vendorFirst ? 0 : 1, auditCount());
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS)); }
    }
    java.util.Map<String, String> callback(com.tmt.ecommerce.payment.entity.Payment payment) {
        var params = new java.util.TreeMap<String, String>();
        params.put("vnp_TxnRef", payment.getTransactionId()); params.put("vnp_Amount", "1000");
        params.put("vnp_ResponseCode", "00"); params.put("vnp_TransactionNo", "test-" + payment.getId());
        String data = params.entrySet().stream().map(e -> e.getKey() + "=" +
                java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.US_ASCII))
                .collect(java.util.stream.Collectors.joining("&"));
        params.put("vnp_SecureHash", com.tmt.ecommerce.payment.util.HashUtil.hmacSha512(secret, data));
        return params;
    }
    @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(booleans = {true, false})
    void adminHoldingOrderDoesNotWaitForPaymentLockedByIpn(boolean priorSuccess) throws Exception {
        state(OrderStatus.CONFIRMED, "VNPAY");
        if (priorSuccess) payment(group(), com.tmt.ecommerce.payment.enums.PaymentMethod.VNPAY, com.tmt.ecommerce.payment.enums.PaymentStatus.SUCCESS);
        var pending = payment(group(), com.tmt.ecommerce.payment.enums.PaymentMethod.VNPAY, com.tmt.ecommerce.payment.enums.PaymentStatus.PENDING);
        var params = callback(pending);
        var pool = Executors.newFixedThreadPool(2); var orderLocked = new CountDownLatch(1); var paymentLocked = new CountDownLatch(1);
        try {
            var admin = pool.submit(() -> {
                Runnable command = () -> tx.executeWithoutResult(s -> {
                    orders.findByIdForUpdate(order).orElseThrow(); orderLocked.countDown(); await(paymentLocked);
                    admin(OrderStatus.CONFIRMED, OrderStatus.SHIPPED);
                });
                if (priorSuccess) command.run();
                else assertEquals(ErrorCode.ORDER_PAYMENT_CONDITION_NOT_MET, assertThrows(AppException.class, command::run).getErrorCode());
            });
            assertTrue(orderLocked.await(10, TimeUnit.SECONDS));
            var ipn = pool.submit(() -> tx.executeWithoutResult(s -> {
                payments.findByTransactionIdForUpdate(pending.getTransactionId()).orElseThrow(); paymentLocked.countDown();
                gateway.processVnPayIpn(params);
            }));

            admin.get(5, TimeUnit.SECONDS); ipn.get(5, TimeUnit.SECONDS);
            assertState(order, priorSuccess ? OrderStatus.SHIPPED : OrderStatus.CONFIRMED, 10, 20, 30);
            assertEquals(priorSuccess ? 1 : 0, auditCount());
            assertEquals(com.tmt.ecommerce.payment.enums.PaymentStatus.SUCCESS, payments.findById(pending.getId()).orElseThrow().getStatus());
        } finally { pool.shutdownNow(); assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS)); }
    }
    @Test void ipnCommitFirstMakesSuccessVisibleAfterAdminWaitsForOrder() throws Exception {
        state(OrderStatus.CONFIRMED, "VNPAY");
        var pending = payment(group(), com.tmt.ecommerce.payment.enums.PaymentMethod.VNPAY, com.tmt.ecommerce.payment.enums.PaymentStatus.PENDING);
        var params = callback(pending); var pool = Executors.newFixedThreadPool(2);
        var locked = new CountDownLatch(1); var release = new CountDownLatch(1); var started = new CountDownLatch(1);
        try {
            var ipn = pool.submit(() -> tx.executeWithoutResult(s -> { gateway.processVnPayIpn(params); locked.countDown(); await(release); }));
            assertTrue(locked.await(10, TimeUnit.SECONDS));
            var admin = pool.submit(() -> { started.countDown(); admin(OrderStatus.CONFIRMED, OrderStatus.SHIPPED); });
            assertTrue(started.await(10, TimeUnit.SECONDS)); assertThrows(TimeoutException.class, () -> admin.get(300, TimeUnit.MILLISECONDS));
            release.countDown(); ipn.get(10, TimeUnit.SECONDS); admin.get(10, TimeUnit.SECONDS);
            assertState(order, OrderStatus.SHIPPED, 10, 20, 30); assertEquals(1, auditCount());
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS)); }
    }
    static void await(CountDownLatch latch) {
        try { assertTrue(latch.await(15, TimeUnit.SECONDS)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }
}
