package com.tmt.ecommerce.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndPhase0ToPhase1Test {

    @LocalServerPort
    private int port;

    private static HttpClient httpClient;
    private static ObjectMapper objectMapper;

    private static String customerToken;
    private static String vendorToken;
    private static Long cartItemId;
    private static Long createdOrderId;
    private static String paymentGroupId;
    private static Long createdReviewId;

    @BeforeAll
    void init() {
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        seedFixtures();
    }

    @org.springframework.beans.factory.annotation.Autowired jakarta.persistence.EntityManager em;
    @org.springframework.beans.factory.annotation.Autowired org.springframework.transaction.PlatformTransactionManager manager;
    private Long productId, fixtureVariantId;
    private String customerEmail, vendorEmail, voucherCode, fixturePrefix;
    private void seedFixtures() {
        fixturePrefix = "e2e-" + java.util.UUID.randomUUID();
        customerEmail = fixturePrefix + "-buyer@example.test";
        vendorEmail = fixturePrefix + "-vendor@example.test";
        voucherCode = fixturePrefix;
        new org.springframework.transaction.support.TransactionTemplate(manager).executeWithoutResult(tx -> {
            var vendorRole = em.createQuery("select r from Role r where r.name='ROLE_VENDOR'", com.tmt.ecommerce.identity.entity.Role.class).getResultStream().findFirst().orElse(null);
            if (vendorRole == null) { vendorRole = com.tmt.ecommerce.identity.entity.Role.builder().name("ROLE_VENDOR").build(); em.persist(vendorRole); }
            var buyerRole = em.createQuery("select r from Role r where r.name='ROLE_USER'", com.tmt.ecommerce.identity.entity.Role.class).getResultStream().findFirst().orElse(null);
            if (buyerRole == null) { buyerRole = com.tmt.ecommerce.identity.entity.Role.builder().name("ROLE_USER").build(); em.persist(buyerRole); }
            String hash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("password123");
            var buyer = com.tmt.ecommerce.identity.entity.User.builder().email(customerEmail).fullName("E2E Buyer").passwordHash(hash).status("ACTIVE").roles(java.util.Set.of(buyerRole)).build(); em.persist(buyer);
            var vendor = com.tmt.ecommerce.identity.entity.User.builder().email(vendorEmail).fullName("E2E Vendor").passwordHash(hash).status("ACTIVE").roles(java.util.Set.of(vendorRole)).build(); em.persist(vendor);
            var shop = com.tmt.ecommerce.shop.entity.Shop.builder().name(fixturePrefix).userId(vendor.getId()).status(com.tmt.ecommerce.shop.enums.ShopStatus.ACTIVE).build(); em.persist(shop);
            var category = com.tmt.ecommerce.product.entity.Category.builder().name(fixturePrefix).build(); em.persist(category);
            for (int i=0; i<10; i++) {
                var product = com.tmt.ecommerce.product.entity.Product.builder().shopId(shop.getId()).category(category).name(fixturePrefix + " Product " + i)
                        .price(new java.math.BigDecimal("100000")).stockQuantity(30).status(com.tmt.ecommerce.product.enums.ProductStatus.ACTIVE).build(); em.persist(product);
                var variant = com.tmt.ecommerce.product.entity.ProductVariant.builder().product(product).sku(fixturePrefix + "-" + i)
                        .price(new java.math.BigDecimal("100000")).stockQuantity(30).status(com.tmt.ecommerce.product.enums.ProductStatus.ACTIVE).build(); em.persist(variant);
                if (i==0) { productId=product.getId(); fixtureVariantId=variant.getId(); }
            }
            em.persist(com.tmt.ecommerce.voucher.entity.Voucher.builder().code(voucherCode).scope(com.tmt.ecommerce.voucher.entity.VoucherScope.SYSTEM)
                    .type(com.tmt.ecommerce.voucher.entity.VoucherType.FIXED_AMOUNT).discountValue(java.math.BigDecimal.TEN)
                    .startDate(java.time.LocalDateTime.now().minusDays(1)).endDate(java.time.LocalDateTime.now().plusDays(1)).usageLimit(100).usedCount(0).build());
        });
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @Order(1)
    @DisplayName("E2E 1. Authentication: Customer and Vendor login")
    void test1_Authentication() throws Exception {
        System.out.println("\n--- [E2E STEP 1] Authentication ---");

        // Customer Login (customer1@gmail.com / password123)
        String customerLoginJson = objectMapper.writeValueAsString(Map.of(
                "email", customerEmail,
                "password", "password123"
        ));
        HttpRequest customerLoginReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/auth/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(customerLoginJson))
                .build();
        HttpResponse<String> customerLoginResp = httpClient.send(customerLoginReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, customerLoginResp.statusCode(), "Customer login should succeed with 200");

        JsonNode customerNode = objectMapper.readTree(customerLoginResp.body());
        customerToken = customerNode.path("data").path("token").asText();
        assertNotNull(customerToken);
        assertFalse(customerToken.isBlank());
        System.out.println("Customer Token acquired: " + customerToken.substring(0, 25) + "...");

        // Vendor Login (vendor1@techzone.vn / password123 - Shop 1: TechZone)
        String vendorLoginJson = objectMapper.writeValueAsString(Map.of(
                "email", vendorEmail,
                "password", "password123"
        ));
        HttpRequest vendorLoginReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/auth/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(vendorLoginJson))
                .build();
        HttpResponse<String> vendorLoginResp = httpClient.send(vendorLoginReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, vendorLoginResp.statusCode(), "Vendor login should succeed with 200");

        JsonNode vendorNode = objectMapper.readTree(vendorLoginResp.body());
        vendorToken = vendorNode.path("data").path("token").asText();
        assertNotNull(vendorToken);
        assertFalse(vendorToken.isBlank());
        System.out.println("Vendor Token acquired: " + vendorToken.substring(0, 25) + "...");
    }

    @Test
    @Order(2)
    @DisplayName("E2E 2. Catalog & Product Detail Browsing")
    void test2_CatalogAndProductDetail() throws Exception {
        System.out.println("\n--- [E2E STEP 2] Catalog & Product Detail ---");

        // 1. Get Product List (Public endpoint)
        HttpRequest listReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/products?page=0&size=10&keyword=" + fixturePrefix)))
                .GET()
                .build();
        HttpResponse<String> listResp = httpClient.send(listReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, listResp.statusCode());

        JsonNode listNode = objectMapper.readTree(listResp.body());
        assertEquals(200, listNode.path("status").asInt());
        JsonNode content = listNode.path("data").path("content");
        assertTrue(content.isArray());
        assertEquals(10, content.size());
        System.out.println("Catalog pagination verified: 10 items returned.");

        // 2. Get Product Detail for Product 1 (iPhone 15 Pro Max)
        HttpRequest detailReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/products/" + productId)))
                .GET()
                .build();
        HttpResponse<String> detailResp = httpClient.send(detailReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, detailResp.statusCode());

        JsonNode detailNode = objectMapper.readTree(detailResp.body());
        assertEquals(fixturePrefix + " Product 0", detailNode.path("data").path("name").asText());
        JsonNode variants = detailNode.path("data").path("variants");
        assertTrue(variants.size() > 0);
        long variantId = variants.get(0).path("id").asLong();
        assertEquals(fixtureVariantId, variantId);
        System.out.println("Product 1 Detail verified. Variant 1 SKU: " + variants.get(0).path("sku").asText());
    }

    @Test
    @Order(3)
    @DisplayName("E2E 3. Cart Flow: Add item to cart and view cart")
    void test3_CartOperations() throws Exception {
        System.out.println("\n--- [E2E STEP 3] Cart Operations ---");

        // 1. Add Variant 1 to Cart
        String cartItemJson = objectMapper.writeValueAsString(Map.of(
                "productVariantId", fixtureVariantId,
                "quantity", 2
        ));
        HttpRequest addCartReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/carts/items")))
                .header("Authorization", "Bearer " + customerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cartItemJson))
                .build();
        HttpResponse<String> addCartResp = httpClient.send(addCartReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, addCartResp.statusCode());

        // 2. View Cart
        HttpRequest getCartReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/carts")))
                .header("Authorization", "Bearer " + customerToken)
                .GET()
                .build();
        HttpResponse<String> getCartResp = httpClient.send(getCartReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getCartResp.statusCode());

        JsonNode cartNode = objectMapper.readTree(getCartResp.body());
        JsonNode items = cartNode.path("data").path("items");
        assertTrue(items.size() > 0);

        for (JsonNode item : items) {
            if (item.path("productVariantId").asLong() == fixtureVariantId) {
                cartItemId = item.path("cartItemId").asLong();
                break;
            }
        }
        assertNotNull(cartItemId, "Cart item with variant 1 should exist in cart");
        assertTrue(cartItemId > 0, "Cart item ID should be greater than 0");
        System.out.println("Cart item created with ID: " + cartItemId);
    }

    @Test
    @Order(4)
    @DisplayName("E2E 4. Checkout Preview with Voucher Calculation")
    void test4_CheckoutPreview() throws Exception {
        System.out.println("\n--- [E2E STEP 4] Checkout Preview ---");

        String previewJson = objectMapper.writeValueAsString(Map.of(
                "cartItemIds", List.of(cartItemId),
                "voucherCode", voucherCode
        ));
        HttpRequest previewReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/orders/preview")))
                .header("Authorization", "Bearer " + customerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(previewJson))
                .build();
        HttpResponse<String> previewResp = httpClient.send(previewReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, previewResp.statusCode());

        JsonNode previewNode = objectMapper.readTree(previewResp.body());
        double subtotal = previewNode.path("data").path("subtotal").asDouble();
        double discount = previewNode.path("data").path("discount").asDouble();
        double total = previewNode.path("data").path("total").asDouble();

        assertTrue(subtotal > 0, "Subtotal should be positive");
        assertTrue(discount > 0, "Voucher WELCOME2026 should apply discount");
        assertEquals(subtotal - discount, total, 0.01, "Total should equal subtotal minus discount");
        System.out.println("Preview calculated: Subtotal=" + subtotal + ", Discount=" + discount + ", Total=" + total);
    }

    @Test
    @Order(5)
    @DisplayName("E2E 5. Order Placement (COD)")
    void test5_CheckoutOrder() throws Exception {
        System.out.println("\n--- [E2E STEP 5] Order Placement (COD) ---");

        String checkoutJson = objectMapper.writeValueAsString(Map.of(
                "shippingAddress", "123 Lê Lợi, Phường Bến Nghé, Quận 1, TP.HCM",
                "paymentMethod", "COD",
                "voucherCode", voucherCode,
                "cartItemIds", List.of(cartItemId)
        ));
        HttpRequest checkoutReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/orders/checkout")))
                .header("Authorization", "Bearer " + customerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(checkoutJson))
                .build();
        HttpResponse<String> checkoutResp = httpClient.send(checkoutReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, checkoutResp.statusCode());

        JsonNode checkoutNode = objectMapper.readTree(checkoutResp.body());
        paymentGroupId = checkoutNode.path("data").path("paymentGroupId").asText();
        assertNotNull(paymentGroupId);

        JsonNode orders = checkoutNode.path("data").path("orders");
        assertTrue(orders.size() > 0);

        JsonNode firstOrder = orders.get(0);
        createdOrderId = firstOrder.path("id").asLong();
        String status = firstOrder.path("status").asText();

        // COD orders automatically transition to CONFIRMED
        assertEquals("CONFIRMED", status);
        System.out.println("Order created: ID=" + createdOrderId + ", Status=" + status + ", PaymentGroup=" + paymentGroupId);
    }

    @Test
    @Order(6)
    @DisplayName("E2E 6. Customer verifies Order History & Details")
    void test6_CustomerOrderHistoryAndDetail() throws Exception {
        System.out.println("\n--- [E2E STEP 6] Customer Order History & Detail ---");

        // 1. Get My Orders
        HttpRequest myOrdersReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/orders?page=0&size=10")))
                .header("Authorization", "Bearer " + customerToken)
                .GET()
                .build();
        HttpResponse<String> myOrdersResp = httpClient.send(myOrdersReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, myOrdersResp.statusCode());

        // 2. Get Order Detail
        HttpRequest detailReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/orders/" + createdOrderId)))
                .header("Authorization", "Bearer " + customerToken)
                .GET()
                .build();
        HttpResponse<String> detailResp = httpClient.send(detailReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, detailResp.statusCode());

        JsonNode detailNode = objectMapper.readTree(detailResp.body());
        assertEquals(createdOrderId, detailNode.path("data").path("id").asLong());
        assertEquals("CONFIRMED", detailNode.path("data").path("status").asText());
        System.out.println("Customer successfully retrieved order details.");
    }

    @Test
    @Order(7)
    @DisplayName("E2E 7. Vendor State Machine: Validation, Shipped & Delivered transitions")
    void test7_VendorStateMachineTransitions() throws Exception {
        System.out.println("\n--- [E2E STEP 7] Vendor State Machine Transitions ---");

        // 1. Vendor lists CONFIRMED orders
        HttpRequest vendorOrdersReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/vendor/orders?status=CONFIRMED")))
                .header("Authorization", "Bearer " + vendorToken)
                .GET()
                .build();
        HttpResponse<String> vendorOrdersResp = httpClient.send(vendorOrdersReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, vendorOrdersResp.statusCode());

        // 2. INVALID Transition Test: CONFIRMED -> DELIVERED (must fail with 400 Bad Request)
        String invalidTransition = objectMapper.writeValueAsString(Map.of(
                "status", "DELIVERED"
        ));
        HttpRequest invalidReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/vendor/orders/" + createdOrderId + "/status")))
                .header("Authorization", "Bearer " + vendorToken)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(invalidTransition))
                .build();
        HttpResponse<String> invalidResp = httpClient.send(invalidReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, invalidResp.statusCode(), "CONFIRMED -> DELIVERED should be rejected with 400 Bad Request");
        System.out.println("State Machine correctly blocked invalid transition: CONFIRMED -> DELIVERED.");

        // 3. VALID Transition: CONFIRMED -> SHIPPED
        String toShipped = objectMapper.writeValueAsString(Map.of(
                "status", "SHIPPED"
        ));
        HttpRequest shippedReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/vendor/orders/" + createdOrderId + "/status")))
                .header("Authorization", "Bearer " + vendorToken)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(toShipped))
                .build();
        HttpResponse<String> shippedResp = httpClient.send(shippedReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, shippedResp.statusCode());

        JsonNode shippedNode = objectMapper.readTree(shippedResp.body());
        assertEquals("SHIPPED", shippedNode.path("data").path("status").asText());
        System.out.println("Order status successfully updated to SHIPPED.");

        // 4. Buyer, not Vendor, confirms receipt: SHIPPED -> DELIVERED.
        HttpRequest deliveredReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/orders/" + createdOrderId + "/confirm-delivery")))
                .header("Authorization", "Bearer " + customerToken)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> deliveredResp = httpClient.send(deliveredReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deliveredResp.statusCode());

        JsonNode deliveredNode = objectMapper.readTree(deliveredResp.body());
        assertEquals("DELIVERED", deliveredNode.path("data").path("status").asText());
        System.out.println("Order status successfully updated to DELIVERED.");
    }

    @Test
    @Order(8)
    @DisplayName("E2E 8. Customer reviews Delivered Product")
    void test8_CustomerReview() throws Exception {
        System.out.println("\n--- [E2E STEP 8] Customer Review ---");

        String reviewJson = objectMapper.writeValueAsString(Map.of(
                "productId", productId,
                "orderId", createdOrderId,
                "rating", 5,
                "comment", "Sản phẩm iPhone 15 Pro Max chính hãng, máy nguyên seal rất đẹp, giao hàng cực nhanh!",
                "imageUrls", List.of("https://images.unsplash.com/photo-1695048133142-1a20484d2569")
        ));
        HttpRequest reviewReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/reviews")))
                .header("Authorization", "Bearer " + customerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reviewJson))
                .build();
        HttpResponse<String> reviewResp = httpClient.send(reviewReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, reviewResp.statusCode());

        JsonNode reviewNode = objectMapper.readTree(reviewResp.body());
        createdReviewId = reviewNode.path("data").path("id").asLong();
        assertTrue(createdReviewId > 0);
        assertEquals(5, reviewNode.path("data").path("rating").asInt());
        System.out.println("Review created with ID: " + createdReviewId);
    }

    @Test
    @Order(9)
    @DisplayName("E2E 9. Vendor replies to Customer Review")
    void test9_VendorReplyReview() throws Exception {
        System.out.println("\n--- [E2E STEP 9] Vendor Reply Review ---");

        String replyJson = objectMapper.writeValueAsString(Map.of(
                "replyComment", "Dạ Shop TechZone cảm ơn quý khách đã tin tưởng và đánh giá 5 sao cho sản phẩm ạ!"
        ));
        HttpRequest replyReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/vendor/reviews/" + createdReviewId + "/reply")))
                .header("Authorization", "Bearer " + vendorToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(replyJson))
                .build();
        HttpResponse<String> replyResp = httpClient.send(replyReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, replyResp.statusCode());

        JsonNode replyNode = objectMapper.readTree(replyResp.body());
        assertEquals("Dạ Shop TechZone cảm ơn quý khách đã tin tưởng và đánh giá 5 sao cho sản phẩm ạ!",
                replyNode.path("data").path("vendorReply").asText());
        System.out.println("Vendor reply saved successfully.");
    }

    @Test
    @Order(10)
    @DisplayName("E2E 10. Verify Public Review Summary & Public Product Reviews")
    void test10_VerifyPublicReviewAndSummary() throws Exception {
        System.out.println("\n--- [E2E STEP 10] Public Review Summary & Product Reviews ---");

        // 1. Rating Summary
        HttpRequest summaryReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/products/" + productId + "/reviews/summary")))
                .GET()
                .build();
        HttpResponse<String> summaryResp = httpClient.send(summaryReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, summaryResp.statusCode());

        JsonNode summaryNode = objectMapper.readTree(summaryResp.body());
        assertTrue(summaryNode.path("data").path("totalReviews").asInt() >= 1);

        // 2. Product Reviews List
        HttpRequest listReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/products/" + productId + "/reviews?page=0&size=10")))
                .GET()
                .build();
        HttpResponse<String> listResp = httpClient.send(listReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, listResp.statusCode());

        JsonNode listNode = objectMapper.readTree(listResp.body());
        assertTrue(listNode.path("data").path("content").size() > 0);
        System.out.println("Public reviews & rating summary validated successfully.");
    }

    @Test
    @Order(11)
    @DisplayName("E2E 11. Vendor Cancelling Confirmed Order")
    void test11_VendorCancelOrder() throws Exception {
        System.out.println("\n--- [E2E STEP 11] Vendor Cancelling Order ---");

        // 1. Customer adds item & checkouts another order
        String cartItemJson = objectMapper.writeValueAsString(Map.of(
                "productVariantId", fixtureVariantId,
                "quantity", 1
        ));
        HttpRequest addCartReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/carts/items")))
                .header("Authorization", "Bearer " + customerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cartItemJson))
                .build();
        httpClient.send(addCartReq, HttpResponse.BodyHandlers.ofString());

        String checkoutJson = objectMapper.writeValueAsString(Map.of(
                "shippingAddress", "456 Hai Bà Trưng, Quận 3, TP.HCM",
                "paymentMethod", "COD",
                "voucherCode", voucherCode
        ));
        HttpRequest checkoutReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/orders/checkout")))
                .header("Authorization", "Bearer " + customerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(checkoutJson))
                .build();
        HttpResponse<String> checkoutResp = httpClient.send(checkoutReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, checkoutResp.statusCode());

        JsonNode checkoutNode = objectMapper.readTree(checkoutResp.body());
        long orderToCancelId = checkoutNode.path("data").path("orders").get(0).path("id").asLong();

        // 2. Vendor cancels this CONFIRMED order
        String cancelJson = objectMapper.writeValueAsString(Map.of(
                "status", "CANCELLED",
                "reason", "Khách hàng thông báo hủy đơn để chọn màu khác"
        ));
        HttpRequest cancelReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/vendor/orders/" + orderToCancelId + "/status")))
                .header("Authorization", "Bearer " + vendorToken)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(cancelJson))
                .build();
        HttpResponse<String> cancelResp = httpClient.send(cancelReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, cancelResp.statusCode());

        JsonNode cancelNode = objectMapper.readTree(cancelResp.body());
        assertEquals("CANCELLED", cancelNode.path("data").path("status").asText());
        System.out.println("Order " + orderToCancelId + " successfully cancelled by Vendor.");
    }

    @Test
    @Order(12)
    @DisplayName("E2E 12. Payment Group Status Query")
    void test12_PaymentGroupStatus() throws Exception {
        System.out.println("\n--- [E2E STEP 12] Payment Group Status Query ---");

        HttpRequest statusReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/v1/payments/group/" + paymentGroupId + "/status")))
                .header("Authorization", "Bearer " + customerToken)
                .GET()
                .build();
        HttpResponse<String> statusResp = httpClient.send(statusReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, statusResp.statusCode());

        JsonNode statusNode = objectMapper.readTree(statusResp.body());
        assertEquals(paymentGroupId, statusNode.path("data").path("paymentGroupId").asText());
        System.out.println("Payment group status query validated successfully.");
    }
}
