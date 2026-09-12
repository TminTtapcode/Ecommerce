package com.tmt.ecommerce.shop;

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

/** Real MySQL, committed fixtures, separate connections/transactions for competing requests. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShopBanEnforcementIntegrationTest {
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Autowired com.tmt.ecommerce.identity.repository.UserRepository users;
    @Autowired com.tmt.ecommerce.identity.repository.RoleRepository roles;
    @Autowired com.tmt.ecommerce.common.security.JwtService jwt;
    @Autowired com.tmt.ecommerce.cart.service.CartService cart;
    @Autowired OrderService service;
    @Autowired com.tmt.ecommerce.shop.service.ShopService shopService;
    @Autowired com.tmt.ecommerce.shop.api.ShopInternalService shopContract;
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
        // Refuse accidental use of the application database before inserting fixtures.
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
    String adminToken;
    Long adminId;
    String admin() {
        if (adminToken == null) {
            var role = roles.findByName("ROLE_ADMIN").orElseGet(() -> roles.save(
                    com.tmt.ecommerce.identity.entity.Role.builder().name("ROLE_ADMIN").build()));
            var user = users.save(com.tmt.ecommerce.identity.entity.User.builder()
                    .email("admin-" + UUID.randomUUID() + "@example.test").passwordHash("unused")
                    .fullName("Test admin").status("ACTIVE").roles(java.util.Set.of(role)).build());
            adminId = user.getId(); adminToken = jwt.generateToken(new com.tmt.ecommerce.identity.security.CustomUserDetails(user));
        }
        return adminToken;
    }
    java.net.http.HttpResponse<String> http(String method, String path, String bearer, String body) throws Exception {
        var builder = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:" + port + path));
        if (bearer != null) builder.header("Authorization", "Bearer " + bearer);
        builder.header("Content-Type", "application/json").method(method, body == null ?
                java.net.http.HttpRequest.BodyPublishers.noBody() : java.net.http.HttpRequest.BodyPublishers.ofString(body));
        return java.net.http.HttpClient.newHttpClient().send(builder.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
    }
    tools.jackson.databind.JsonNode body(java.net.http.HttpResponse<String> response, int status) {
        assertEquals(status, response.statusCode(), response.body());
        return tools.jackson.databind.json.JsonMapper.builder().build().readTree(response.body());
    }
    void error(java.net.http.HttpResponse<String> response, int status, String code) {
        var result = body(response, status); assertFalse(result.get("success").asBoolean());
        assertEquals(code, result.get("errorCode").asText()); assertTrue(result.get("data").isNull());
    }
    String shopPath(String action) { return "/api/v1/admin/shops/" + shop + "/" + action; }
    void ban() throws Exception { body(http("PUT", shopPath("ban"), admin(), "{\"reason\":\"Test restriction\"}"), 200); }
    void unban() throws Exception { body(http("PUT", shopPath("unban"), admin(), null), 200); }
    void pending() {
        tx.executeWithoutResult(s -> {
            shops.findById(shop).orElseThrow().setStatus(ShopStatus.PENDING);
            users.findById(owner).orElseThrow().getRoles().clear();
        });
    }
    boolean hasVendor() { return users.findById(owner).orElseThrow().getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_VENDOR")); }
    Long addCart() throws Exception {
        body(http("POST", "/api/v1/carts/items", token, "{\"productVariantId\":" + v1 + ",\"quantity\":2}"), 200);
        return cart.getCart(owner).items().getFirst().cartItemId();
    }
    String checkoutBody() { return "{\"shippingAddress\":\"Test\",\"paymentMethod\":\"COD\"}"; }
    @Test void activeBanUnbanPreservesPriorAndRole() throws Exception {
        ban(); var banned = shops.findById(shop).orElseThrow();
        assertEquals(ShopStatus.BANNED, banned.getStatus()); assertEquals(ShopStatus.ACTIVE, banned.getPriorStatus());
        assertTrue(hasVendor()); unban();
        assertEquals(ShopStatus.ACTIVE, shops.findById(shop).orElseThrow().getStatus());
        assertNull(shops.findById(shop).orElseThrow().getPriorStatus()); assertTrue(hasVendor());
    }
    @Test void pendingBanUnbanDoesNotApproveOrGrantRole() throws Exception {
        pending(); ban(); assertEquals(ShopStatus.PENDING, shops.findById(shop).orElseThrow().getPriorStatus());
        error(http("PUT", shopPath("approve"), admin(), null), 400, "SHOP_INVALID_STATUS_TRANSITION");
        unban(); assertEquals(ShopStatus.PENDING, shops.findById(shop).orElseThrow().getStatus()); assertFalse(hasVendor());
        body(http("PUT", shopPath("approve"), admin(), null), 200); assertTrue(hasVendor());
        assertEquals(ShopStatus.ACTIVE, shops.findById(shop).orElseThrow().getStatus());
    }
    @Test void legacyBanCannotBeUnbannedAutomatically() throws Exception {
        tx.executeWithoutResult(s -> shops.findById(shop).orElseThrow().setStatus(ShopStatus.BANNED));
        error(http("PUT", shopPath("unban"), admin(), null), 409, "SHOP_PRIOR_STATUS_MISSING");
        assertEquals(ShopStatus.BANNED, shops.findById(shop).orElseThrow().getStatus());
    }
    @Test void nonAdminCannotBanOrUnban() throws Exception {
        error(http("PUT", shopPath("ban"), token, null), 403, "ACCESS_DENIED");
        error(http("PUT", shopPath("unban"), token, null), 403, "ACCESS_DENIED");
        assertEquals(ShopStatus.ACTIVE, shops.findById(shop).orElseThrow().getStatus());
    }
    @Test void duplicateTransitionsAndMissingShopReturnCodes() throws Exception {
        error(http("PUT", shopPath("unban"), admin(), null), 400, "SHOP_INVALID_STATUS_TRANSITION");
        ban(); error(http("PUT", shopPath("ban"), admin(), null), 400, "SHOP_INVALID_STATUS_TRANSITION");
        error(http("PUT", "/api/v1/admin/shops/" + Long.MAX_VALUE + "/ban", admin(), null), 404, "SHOP_NOT_FOUND");
    }
    @Test void optionalReasonAndLengthValidation() throws Exception {
        error(http("PUT", shopPath("ban"), admin(), "{\"reason\":\"" + "x".repeat(501) + "\"}"), 400, "INVALID_INPUT");
        body(http("PUT", shopPath("ban"), admin(), null), 200);
        assertEquals(ShopStatus.BANNED, shops.findById(shop).orElseThrow().getStatus());
    }
    @Test void publicCatalogFiltersBeforePagingAndDetailIsHidden() throws Exception {
        String name = "catalog-" + UUID.randomUUID();
        var other = shops.save(Shop.builder().userId(adminUserId()).name("other-" + UUID.randomUUID()).status(ShopStatus.ACTIVE).build());
        tx.executeWithoutResult(s -> {
            var original = products.findById(product).orElseThrow(); original.setName(name);
            products.save(Product.builder().shopId(other.getId()).category(original.getCategory()).name(name)
                    .price(BigDecimal.TEN).stockQuantity(1).status(ProductStatus.ACTIVE).build());
        });
        ban();
        var page = body(http("GET", "/api/v1/products?size=1&keyword=" + name, null, null), 200).get("data");
        assertEquals(1, page.get("totalElements").asInt()); assertEquals(1, page.get("content").size());
        assertEquals(other.getId(), page.get("content").get(0).get("shopId").asLong());
        error(http("GET", "/api/v1/products/" + product, null, null), 404, "PRODUCT_NOT_FOUND");
    }
    Long adminUserId() { admin(); return adminId; }
    @Test void ownProductReadableButOtherVendorCannotReadIt() throws Exception {
        ban(); body(http("GET", "/api/v1/vendor/products/" + product, token, null), 200);
        error(http("GET", "/api/v1/vendor/products/" + product, admin(), null), 403, "ACCESS_DENIED");
    }
    @Test void bothProductApiAliasesRejectAllSalesMutations() throws Exception {
        Long categoryId = tx.execute(s -> products.findById(product).orElseThrow().getCategory().getId());
        String payload = "{\"name\":\"Changed\",\"price\":10,\"stockQuantity\":30,\"categoryId\":" + categoryId + "}";
        ban();
        for (String base : List.of("/api/v1/products", "/api/v1/vendor/products")) {
            error(http("POST", base, token, payload), 403, "SHOP_NOT_ACTIVE");
            error(http("PUT", base + "/" + product, token, payload), 403, "SHOP_NOT_ACTIVE");
            error(http("DELETE", base + "/" + product, token, null), 403, "SHOP_NOT_ACTIVE");
        }
        assertEquals("Cancellation fixture", products.findById(product).orElseThrow().getName());
    }
    @Test void bannedCartKeepsItemsAllowsDecreaseAndDeleteButRejectsNewOrIncrease() throws Exception {
        Long item = addCart(); ban();
        var items = body(http("GET", "/api/v1/carts", token, null), 200).get("data").get("items");
        assertEquals(1, items.size()); assertFalse(items.get(0).get("isAvailable").asBoolean());
        error(http("POST", "/api/v1/carts/items", token, "{\"productVariantId\":" + v2 + ",\"quantity\":1}"), 403, "SHOP_NOT_ACTIVE");
        error(http("PUT", "/api/v1/carts/items/" + item, token, "{\"quantity\":3}"), 403, "SHOP_NOT_ACTIVE");
        body(http("PUT", "/api/v1/carts/items/" + item, token, "{\"quantity\":1}"), 200);
        body(http("DELETE", "/api/v1/carts/items/" + item, token, null), 200);
        assertTrue(cart.getCart(owner).items().isEmpty());
    }
    @Test void mixedCheckoutWithBannedItemRejectsWholeSelection() throws Exception {
        addCart();
        var other = shops.save(Shop.builder().userId(adminUserId()).name("other-" + UUID.randomUUID()).status(ShopStatus.ACTIVE).build());
        Long variant = tx.execute(s -> {
            var p = products.save(Product.builder().shopId(other.getId()).category(products.findById(product).orElseThrow().getCategory())
                    .name("Other").price(BigDecimal.TEN).stockQuantity(10).status(ProductStatus.ACTIVE).build());
            return variant(p, 10);
        });
        body(http("POST", "/api/v1/carts/items", token, "{\"productVariantId\":" + variant + ",\"quantity\":1}"), 200);
        long count = orders.count(); ban();
        error(http("POST", "/api/v1/orders/checkout", token, checkoutBody()), 403, "SHOP_NOT_ACTIVE");
        assertEquals(count, orders.count()); assertEquals(2, cart.getCart(owner).items().size());
        assertEquals(10, variants.findById(variant).orElseThrow().getStockQuantity());
        assertState(order, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void existingOrdersRemainReadableCancellableAndShippable() throws Exception {
        Long shipping = newOrder(OrderStatus.CONFIRMED, v1, 2, v2, 3); ban();
        body(http("GET", "/api/v1/vendor/orders/" + order, token, null), 200);
        body(http("PATCH", "/api/v1/vendor/orders/" + order + "/status", token, "{\"status\":\"CANCELLED\"}"), 200);
        body(http("PATCH", "/api/v1/vendor/orders/" + shipping + "/status", token, "{\"status\":\"SHIPPED\"}"), 200);
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @Test void unbanDoesNotReactivateHiddenProduct() throws Exception {
        tx.executeWithoutResult(s -> products.findById(product).orElseThrow().setStatus(ProductStatus.HIDDEN));
        ban(); unban(); assertEquals(ProductStatus.HIDDEN, products.findById(product).orElseThrow().getStatus());
        error(http("GET", "/api/v1/products/" + product, null, null), 404, "PRODUCT_NOT_FOUND");
    }
    @Test void unbanActiveRestoresCartAvailabilityAndCheckout() throws Exception {
        addCart(); ban(); unban();
        body(http("GET", "/api/v1/products/" + product, null, null), 200);
        assertTrue(cart.getCart(owner).items().getFirst().isAvailable());
        body(http("POST", "/api/v1/orders/checkout", token, checkoutBody()), 201);
        assertState(order, OrderStatus.CONFIRMED, 8, 20, 28);
    }
    @Test void bannedVendorCannotCreateShopVoucher() throws Exception {
        ban();
        String payload = "{\"code\":\"TEST-" + UUID.randomUUID() + "\",\"scope\":\"SHOP\",\"type\":\"FIXED_AMOUNT\","
                + "\"discountValue\":1,\"startDate\":\"" + java.time.LocalDateTime.now().minusDays(1)
                + "\",\"endDate\":\"" + java.time.LocalDateTime.now().plusDays(1) + "\"}";
        error(http("POST", "/api/v1/vouchers", token, payload), 403, "SHOP_NOT_ACTIVE");
    }
    @Test void duplicateConcurrentBanDoesNotOverwritePriorStatus() throws Exception {
        admin(); race(() -> shops.findByIdForUpdate(shop).orElseThrow(), () -> shopService.banShop(adminId, shop, null),
                () -> assertThrows(AppException.class, () -> shopService.banShop(adminId, shop, null)));
        assertEquals(ShopStatus.ACTIVE, shops.findById(shop).orElseThrow().getPriorStatus());
        assertEquals(ShopStatus.BANNED, shops.findById(shop).orElseThrow().getStatus());
    }
    @Test void rollbackRestoresStatusPriorAndSuppressesAuditEvent() {
        admin(); tx.executeWithoutResult(s -> { shopService.banShop(adminId, shop, "rollback"); s.setRollbackOnly(); });
        assertEquals(ShopStatus.ACTIVE, shops.findById(shop).orElseThrow().getStatus());
        assertNull(shops.findById(shop).orElseThrow().getPriorStatus()); assertFalse(probe.records.containsKey(shop));
    }
    @Test void auditHasActorOldNewAndOptionalReasonAfterCommit() throws Exception {
        ban(); var event = probe.records.get(shop);
        assertNotNull(event); assertEquals(adminId, event.actorId()); assertEquals(ShopStatus.ACTIVE, event.oldStatus());
        assertEquals(ShopStatus.BANNED, event.newStatus()); assertEquals("Test restriction", event.reason());
    }
    @Test void banWinsAgainstCheckout() throws Exception {
        addCart(); admin(); long count = orders.count();
        race(() -> shops.findByIdForUpdate(shop).orElseThrow(), () -> shopService.banShop(adminId, shop, null),
                () -> assertEquals(ErrorCode.SHOP_NOT_ACTIVE, assertThrows(AppException.class, () -> service.checkout(owner,
                        new com.tmt.ecommerce.order.dto.request.CheckoutRequest("Test", "COD", null, null))).getErrorCode()));
        assertEquals(count, orders.count()); assertState(order, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void checkoutWinsThenBanWaitsForCommit() throws Exception {
        addCart(); admin(); long count = orders.count();
        race(() -> shopContract.requireNotBannedForSale(shop), () -> service.checkout(owner,
                new com.tmt.ecommerce.order.dto.request.CheckoutRequest("Test", "COD", null, null)),
                () -> shopService.banShop(adminId, shop, null));
        assertEquals(count + 1, orders.count()); assertEquals(ShopStatus.BANNED, shops.findById(shop).orElseThrow().getStatus());
        assertState(order, OrderStatus.CONFIRMED, 8, 20, 28);
    }
    @Test void banBeforeApprovalPreventsRoleGrant() throws Exception {
        pending(); admin();
        race(() -> shops.findByIdForUpdate(shop).orElseThrow(), () -> shopService.banShop(adminId, shop, null),
                () -> assertThrows(AppException.class, () -> shopService.approveShop(shop)));
        assertFalse(hasVendor()); assertEquals(ShopStatus.PENDING, shops.findById(shop).orElseThrow().getPriorStatus());
    }
    @Test void approvalBeforeBanSavesActivePriorStatus() throws Exception {
        pending(); admin();
        race(() -> shops.findByIdForUpdate(shop).orElseThrow(), () -> shopService.approveShop(shop),
                () -> shopService.banShop(adminId, shop, null));
        assertTrue(hasVendor()); assertEquals(ShopStatus.ACTIVE, shops.findById(shop).orElseThrow().getPriorStatus());
    }
    @Test void concurrentBanUnbanRestoresOriginalStatus() throws Exception {
        admin(); race(() -> shops.findByIdForUpdate(shop).orElseThrow(), () -> shopService.banShop(adminId, shop, null),
                () -> shopService.unbanShop(adminId, shop));
        assertEquals(ShopStatus.ACTIVE, shops.findById(shop).orElseThrow().getStatus());
        assertNull(shops.findById(shop).orElseThrow().getPriorStatus());
    }
    void race(Runnable lock, Runnable finish, Runnable competitor) throws Exception {
        var pool = Executors.newFixedThreadPool(2); var locked = new CountDownLatch(1);
        var release = new CountDownLatch(1); var started = new CountDownLatch(1);
        try {
            var first = pool.submit(() -> tx.executeWithoutResult(s -> { lock.run(); locked.countDown(); await(release); finish.run(); }));
            assertTrue(locked.await(10, TimeUnit.SECONDS));
            var second = pool.submit(() -> { started.countDown(); competitor.run(); });
            assertTrue(started.await(10, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> second.get(300, TimeUnit.MILLISECONDS));
            release.countDown(); first.get(15, TimeUnit.SECONDS); second.get(15, TimeUnit.SECONDS);
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS)); }
    }
    static void await(CountDownLatch latch) {
        try { if (!latch.await(15, TimeUnit.SECONDS)) throw new AssertionError("Latch timeout"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }
    @Autowired AuditProbe probe;
    @org.springframework.boot.test.context.TestConfiguration
    static class ProbeConfig {
        @org.springframework.context.annotation.Bean AuditProbe auditProbe() { return new AuditProbe(); }
    }
    static class AuditProbe {
        final java.util.concurrent.ConcurrentMap<Long, com.tmt.ecommerce.shop.api.event.ShopStatusChangedEvent> records = new ConcurrentHashMap<>();
        @org.springframework.transaction.event.TransactionalEventListener
        public void changed(com.tmt.ecommerce.shop.api.event.ShopStatusChangedEvent e) { records.put(e.shopId(), e); }
    }
}
