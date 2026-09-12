package com.tmt.ecommerce.order;

import com.tmt.ecommerce.identity.entity.Role;
import com.tmt.ecommerce.identity.entity.User;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Direct HTTP tests on MySQL for D8 ownership, state and durable delivery evidence. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeliveryConfirmationIntegrationTest {
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired com.tmt.ecommerce.identity.repository.UserRepository users;
    @Autowired com.tmt.ecommerce.identity.repository.RoleRepository roles;
    @Autowired com.tmt.ecommerce.common.security.JwtService jwt;
    @Autowired ShopRepository shops;
    @Autowired OrderRepository orders;

    Long buyerId, otherBuyerId, vendorId, adminId, shopId, orderId;
    String buyerToken, otherBuyerToken, vendorToken, adminToken;

    @BeforeEach void setup() {
        assertTrue(jdbc.queryForObject("select database()", String.class).startsWith("ecommerce_s103_s104_s105"));
        User buyer = user("ROLE_USER", "buyer");
        User otherBuyer = user("ROLE_USER", "other-buyer");
        User vendor = user("ROLE_VENDOR", "vendor");
        User admin = user("ROLE_ADMIN", "admin");
        buyerId = buyer.getId(); otherBuyerId = otherBuyer.getId(); vendorId = vendor.getId(); adminId = admin.getId();
        buyerToken = token(buyer); otherBuyerToken = token(otherBuyer); vendorToken = token(vendor); adminToken = token(admin);
        shopId = shops.save(Shop.builder().name("p301-" + UUID.randomUUID()).userId(vendorId).status(ShopStatus.ACTIVE).build()).getId();
        orderId = orders.save(Order.builder().userId(buyerId).shopId(shopId).paymentGroupId("p301-" + UUID.randomUUID())
                .status(OrderStatus.SHIPPED).paymentMethod("COD").shippingAddress("P3 address")
                .originalTotalAmount(BigDecimal.TEN).discountAmount(BigDecimal.ZERO).totalAmount(BigDecimal.TEN).build()).getId();
    }

    private User user(String roleName, String label) {
        Role role = roles.findByName(roleName).orElseGet(() -> roles.save(Role.builder().name(roleName).build()));
        return users.save(User.builder().email("p301-" + label + "-" + UUID.randomUUID() + "@example.test")
                .fullName(label).passwordHash("test").status("ACTIVE").roles(Set.of(role)).build());
    }

    private String token(User user) {
        return jwt.generateToken(new com.tmt.ecommerce.identity.security.CustomUserDetails(user));
    }

    private HttpResponse<String> call(String method, String path, String token, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) request.header("Authorization", "Bearer " + token);
        return HttpClient.newHttpClient().send(request.method(method,
                body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private void error(HttpResponse<String> response, int status, String code) throws Exception {
        assertEquals(status, response.statusCode(), response.body());
        var json = tools.jackson.databind.json.JsonMapper.builder().build().readTree(response.body());
        assertFalse(json.get("success").asBoolean());
        assertEquals(code, json.get("errorCode").asText());
        assertTrue(json.get("data").isNull());
    }

    @Test void buyerConfirmsOnlyOwnShippedOrderAndWritesEvidence() throws Exception {
        assertEquals(200, call("PATCH", "/api/v1/orders/" + orderId + "/confirm-delivery", buyerToken, null).statusCode());
        Order updated = orders.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, updated.getStatus());
        assertNotNull(updated.getDeliveredAt());
        assertEquals(buyerId, updated.getDeliveryConfirmedByUserId());
        assertEquals(com.tmt.ecommerce.order.entity.DeliveryConfirmationSource.BUYER, updated.getDeliveryConfirmationSource());
    }

    @Test void foreignBuyerAndUnauthenticatedRequestCannotMutateOrLearnOrder() throws Exception {
        error(call("PATCH", "/api/v1/orders/" + orderId + "/confirm-delivery", null, null), 401, "UNAUTHORIZED");
        error(call("PATCH", "/api/v1/orders/" + orderId + "/confirm-delivery", otherBuyerToken, null), 404, "ORDER_NOT_FOUND");
        assertEquals(OrderStatus.SHIPPED, orders.findById(orderId).orElseThrow().getStatus());
    }

    @Test void duplicateAndNonShippedConfirmationsAreRejectedWithoutRewritingEvidence() throws Exception {
        assertEquals(200, call("PATCH", "/api/v1/orders/" + orderId + "/confirm-delivery", buyerToken, null).statusCode());
        var first = orders.findById(orderId).orElseThrow().getDeliveredAt();
        error(call("PATCH", "/api/v1/orders/" + orderId + "/confirm-delivery", buyerToken, null), 400, "INVALID_ORDER_STATE_TRANSITION");
        assertEquals(first, orders.findById(orderId).orElseThrow().getDeliveredAt());
        Order pending = orders.save(Order.builder().userId(buyerId).shopId(shopId).paymentGroupId("p301-" + UUID.randomUUID())
                .status(OrderStatus.PENDING).paymentMethod("COD").shippingAddress("P3 address")
                .originalTotalAmount(BigDecimal.TEN).discountAmount(BigDecimal.ZERO).totalAmount(BigDecimal.TEN).build());
        error(call("PATCH", "/api/v1/orders/" + pending.getId() + "/confirm-delivery", buyerToken, null), 400, "INVALID_ORDER_STATE_TRANSITION");
        assertNull(orders.findById(pending.getId()).orElseThrow().getDeliveredAt());
    }

    @Test void concurrentBuyerConfirmationsCommitOnceAndSendOneDeliveredNotification() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> call("PATCH", "/api/v1/orders/" + orderId + "/confirm-delivery", buyerToken, null));
            var second = pool.submit(() -> call("PATCH", "/api/v1/orders/" + orderId + "/confirm-delivery", buyerToken, null));
            int a = first.get(15, TimeUnit.SECONDS).statusCode();
            int b = second.get(15, TimeUnit.SECONDS).statusCode();
            assertTrue((a == 200 && b == 400) || (a == 400 && b == 200), a + "," + b);
            assertEquals(OrderStatus.DELIVERED, orders.findById(orderId).orElseThrow().getStatus());
            assertEquals(1, jdbc.queryForObject("select count(*) from notifications where reference_id = ? and type = 'ORDER_DELIVERED'",
                    Integer.class, orderId.toString()));
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    @Test void vendorCannotMarkDeliveredThroughVendorStatusEndpoint() throws Exception {
        error(call("PATCH", "/api/v1/vendor/orders/" + orderId + "/status", vendorToken,
                "{\"status\":\"DELIVERED\"}"), 400, "INVALID_ORDER_STATE_TRANSITION");
        assertEquals(OrderStatus.SHIPPED, orders.findById(orderId).orElseThrow().getStatus());
        assertNull(orders.findById(orderId).orElseThrow().getDeliveredAt());
    }

    @Test void adminForceCompleteRetainsD2AuditAndWritesAdminEvidence() throws Exception {
        var response = call("PATCH", "/api/v1/admin/orders/" + orderId + "/status", adminToken,
                "{\"expectedStatus\":\"SHIPPED\",\"status\":\"DELIVERED\",\"reason\":\"Force complete after dispute review\"}");
        assertEquals(200, response.statusCode(), response.body());
        Order updated = orders.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, updated.getStatus());
        assertEquals(adminId, updated.getDeliveryConfirmedByUserId());
        assertEquals(com.tmt.ecommerce.order.entity.DeliveryConfirmationSource.ADMIN, updated.getDeliveryConfirmationSource());
        assertEquals(1, jdbc.queryForObject("select count(*) from order_admin_status_history where order_id = ?", Integer.class, orderId));
    }

    @Test void buyerCanConfirmShippedOrderOfBannedShop() throws Exception {
        shops.findById(shopId).orElseThrow().setStatus(ShopStatus.BANNED);
        assertEquals(200, call("PATCH", "/api/v1/orders/" + orderId + "/confirm-delivery", buyerToken, null).statusCode());
        assertEquals(OrderStatus.DELIVERED, orders.findById(orderId).orElseThrow().getStatus());
    }
}
