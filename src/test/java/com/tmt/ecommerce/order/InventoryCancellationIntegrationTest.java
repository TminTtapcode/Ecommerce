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

/** Real MySQL, committed fixtures, separate connections/transactions for competing requests. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryCancellationIntegrationTest {
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
    TransactionTemplate tx;
    Long owner, shop, product, v1, v2, order;
    String token;

    @BeforeEach void setup() {
        // Refuse accidental use of the application database before inserting fixtures.
        assertTrue(jdbc.queryForObject("select database()", String.class).startsWith("ecommerce_s103"));
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
    @Test void restoresMultipleVariantsAndRejectsDuplicate() {
        transition(order, OrderStatus.CANCELLED);
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
        assertEquals(ErrorCode.INVALID_ORDER_STATE_TRANSITION,
                assertThrows(AppException.class, () -> transition(order, OrderStatus.CANCELLED)).getErrorCode());
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PENDING", "SHIPPED", "DELIVERED", "CANCELLED"})
    void rejectsInvalidSourceStates(OrderStatus state) {
        tx.executeWithoutResult(s -> orders.findById(order).orElseThrow().setStatus(state));
        assertEquals(ErrorCode.INVALID_ORDER_STATE_TRANSITION,
                assertThrows(AppException.class, () -> transition(order, OrderStatus.CANCELLED)).getErrorCode());
        assertState(order, state, 10, 20, 30);
    }
    @Test void deniesOtherShopOwner() {
        assertThrows(AccessDeniedException.class, () -> service.updateVendorOrderStatus(owner - 1, order,
                new VendorOrderStatusUpdateRequest(OrderStatus.CANCELLED, null)));
        assertState(order, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void missingSecondVariantRollsBackFirstItemAndStatus() {
        Long broken = newOrder(OrderStatus.CONFIRMED, v1, 2, Long.MAX_VALUE, 3);
        assertEquals(ErrorCode.VARIANT_NOT_FOUND,
                assertThrows(AppException.class, () -> transition(broken, OrderStatus.CANCELLED)).getErrorCode());
        assertState(broken, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void invalidSecondQuantityRollsBackFirstItemAndStatus() {
        Long broken = newOrder(OrderStatus.CONFIRMED, v1, 2, v2, 0);
        assertThrows(AppException.class, () -> transition(broken, OrderStatus.CANCELLED));
        assertState(broken, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void databaseFailureOnSecondProductUpdateRollsBackEverything() {
        // First restoration fits, second exceeds the real INT column. Exercises DB failure after variant UPDATE.
        jdbc.update("update products set stock_quantity = ? where id = ?", Integer.MAX_VALUE - 2, product);
        assertThrows(RuntimeException.class, () -> transition(order, OrderStatus.CANCELLED));
        assertState(order, OrderStatus.CONFIRMED, 10, 20, Integer.MAX_VALUE - 2);
    }
    @Test void callerRollbackAlsoRollsBackCancellationAndStock() {
        tx.executeWithoutResult(s -> { transition(order, OrderStatus.CANCELLED); s.setRollbackOnly(); });
        assertState(order, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void bannedShopAndHiddenProductStillRestore() {
        tx.executeWithoutResult(s -> {
            shops.findById(shop).orElseThrow().setStatus(ShopStatus.BANNED);
            products.findById(product).orElseThrow().setStatus(ProductStatus.HIDDEN);
        });
        transition(order, OrderStatus.CANCELLED);
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @Test void replayedEventDoesNotRestoreStock() {
        transition(order, OrderStatus.CANCELLED);
        tx.executeWithoutResult(s -> {
            var event = new OrderCancelledEvent(order, owner, shop, List.of(new OrderItemCancelDto(v1, 2)));
            events.publishEvent(event); events.publishEvent(event);
        });
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @Test void restorationRequiresCallerTransaction() {
        assertThrows(org.springframework.transaction.IllegalTransactionStateException.class,
                () -> inventory.restoreStock(v1, 2));
        assertState(order, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void checkoutThenCancelRestoresOriginalStock() {
        for (var item : List.of(new OrderItemCancelDto(v1, 2), new OrderItemCancelDto(v2, 3))) {
            var request = new com.tmt.ecommerce.cart.dto.request.CartItemRequest();
            request.setProductVariantId(item.variantId()); request.setQuantity(item.quantity());
            cart.addToCart(owner, request);
        }
        var result = service.checkout(owner, new com.tmt.ecommerce.order.dto.request.CheckoutRequest(
                "Test", "COD", null, null));
        Long checkedOut = result.getOrders().getFirst().id();
        assertState(checkedOut, OrderStatus.PENDING, 8, 17, 25);
        transition(checkedOut, OrderStatus.CONFIRMED);
        assertState(checkedOut, OrderStatus.CONFIRMED, 8, 17, 25);
        transition(checkedOut, OrderStatus.CANCELLED);
        assertState(checkedOut, OrderStatus.CANCELLED, 10, 20, 30);
    }
    @Test void httpCancellationAndDuplicateUseErrorContract() throws Exception {
        assertEquals(200, patch(token, order).statusCode());
        assertHttpError(patch(token, order), 400, "INVALID_ORDER_STATE_TRANSITION");
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @Test void httpOwnerWithoutVendorRoleIsDenied() throws Exception {
        var user = users.findById(owner).orElseThrow();
        user.setRoles(java.util.Set.of()); users.save(user);
        assertHttpError(patch(token, order), 403, "ACCESS_DENIED");
        assertState(order, OrderStatus.CONFIRMED, 10, 20, 30);
    }
    @Test void httpMissingOrderUsesDomainCode() throws Exception {
        assertHttpError(patch(token, Long.MAX_VALUE), 404, "ORDER_NOT_FOUND");
    }
    java.net.http.HttpResponse<String> patch(String bearer, Long id) throws Exception {
        var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                "http://127.0.0.1:" + port + "/api/v1/vendor/orders/" + id + "/status"))
                .header("Authorization", "Bearer " + bearer).header("Content-Type", "application/json")
                .method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofString("{\"status\":\"CANCELLED\"}"))
                .build();
        return java.net.http.HttpClient.newHttpClient().send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
    }
    void assertHttpError(java.net.http.HttpResponse<String> response, int status, String code) {
        assertEquals(status, response.statusCode(), response.body());
        var body = tools.jackson.databind.json.JsonMapper.builder().build().readTree(response.body());
        assertFalse(body.get("success").asBoolean());
        assertEquals(status, body.get("status").asInt());
        assertEquals(code, body.get("errorCode").asText());
        assertTrue(body.has("data") && body.get("data").isNull());
    }
    @Test void concurrentCancellationRestoresExactlyOnce() throws Exception {
        race(OrderStatus.CANCELLED, OrderStatus.CANCELLED);
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @Test void cancellationWinsAgainstShipping() throws Exception {
        race(OrderStatus.CANCELLED, OrderStatus.SHIPPED);
        assertState(order, OrderStatus.CANCELLED, 12, 23, 35);
    }
    @Test void shippingWinsAgainstCancellation() throws Exception {
        race(OrderStatus.SHIPPED, OrderStatus.CANCELLED);
        assertState(order, OrderStatus.SHIPPED, 10, 20, 30);
    }
    void race(OrderStatus first, OrderStatus second) throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var locked = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var started = new CountDownLatch(1);
        try {
            Future<?> winner = pool.submit(() -> tx.executeWithoutResult(s -> {
                orders.findByIdForUpdate(order).orElseThrow();
                locked.countDown();
                await(release);
                transition(order, first);
            }));
            assertTrue(locked.await(10, TimeUnit.SECONDS));
            Future<?> loser = pool.submit(() -> {
                started.countDown();
                assertEquals(ErrorCode.INVALID_ORDER_STATE_TRANSITION,
                        assertThrows(AppException.class, () -> transition(order, second)).getErrorCode());
            });
            assertTrue(started.await(10, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> loser.get(300, TimeUnit.MILLISECONDS),
                    "Competing transaction must wait for the Order lock");
            release.countDown();
            winner.get(15, TimeUnit.SECONDS); loser.get(15, TimeUnit.SECONDS);
        } finally {
            release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS));
        }
    }
    static void await(CountDownLatch latch) {
        try { if (!latch.await(15, TimeUnit.SECONDS)) throw new AssertionError("Latch timed out"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }
}
