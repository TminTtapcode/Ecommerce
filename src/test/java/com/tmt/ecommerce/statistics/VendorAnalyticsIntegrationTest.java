package com.tmt.ecommerce.statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmt.ecommerce.common.security.JwtService;
import com.tmt.ecommerce.identity.entity.Role;
import com.tmt.ecommerce.identity.entity.User;
import com.tmt.ecommerce.identity.repository.RoleRepository;
import com.tmt.ecommerce.identity.repository.UserRepository;
import com.tmt.ecommerce.identity.security.CustomUserDetails;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** HTTP/MySQL evidence for the vendor-only, shop-scoped analytics endpoint. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VendorAnalyticsIntegrationTest {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired ShopRepository shops;
    @Autowired OrderRepository orders;
    @Autowired JwtService jwt;
    @Autowired JdbcTemplate jdbc;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();
    private String vendorAToken;
    private String vendorBToken;
    private String buyerToken;
    private String adminToken;
    private String vendorWithoutShopToken;
    private Long buyerId;
    private Long shopA;
    private Long shopB;

    @BeforeEach
    void setUp() {
        String db = jdbc.queryForObject("SELECT DATABASE()", String.class);
        assertNotNull(db);
        assertTrue(db.contains("ecommerce_"), "Must run against an isolated test schema, got: " + db);

        Role vendor = role("ROLE_VENDOR");
        Role buyer = role("ROLE_USER");
        Role admin = role("ROLE_ADMIN");
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        User vendorA = user("vendor-a-" + suffix, Set.of(vendor));
        User vendorB = user("vendor-b-" + suffix, Set.of(vendor));
        User noShopVendor = user("vendor-none-" + suffix, Set.of(vendor));
        User buyerUser = user("buyer-" + suffix, Set.of(buyer));
        User adminUser = user("admin-" + suffix, Set.of(admin));
        vendorAToken = jwt.generateToken(new CustomUserDetails(vendorA));
        vendorBToken = jwt.generateToken(new CustomUserDetails(vendorB));
        vendorWithoutShopToken = jwt.generateToken(new CustomUserDetails(noShopVendor));
        buyerToken = jwt.generateToken(new CustomUserDetails(buyerUser));
        adminToken = jwt.generateToken(new CustomUserDetails(adminUser));
        buyerId = buyerUser.getId();
        shopA = shops.save(Shop.builder().name("Analytics A " + suffix).userId(vendorA.getId()).status(ShopStatus.ACTIVE).build()).getId();
        shopB = shops.save(Shop.builder().name("Analytics B " + suffix).userId(vendorB.getId()).status(ShopStatus.ACTIVE).build()).getId();
    }

    @Test
    void ownShopMetricsAreScopedAndAggregateDeliveredVoucherSnapshots() throws Exception {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        create(shopA, OrderStatus.PENDING, "20", "0", "20", now);
        create(shopA, OrderStatus.CONFIRMED, "30", "0", "30", now);
        create(shopA, OrderStatus.SHIPPED, "40", "0", "40", now);
        create(shopA, OrderStatus.DELIVERED, "100", "10", "90", now);
        create(shopA, OrderStatus.DELIVERED, "200", "20", "180", now);
        create(shopA, OrderStatus.CANCELLED, "50", "5", "45", now);
        create(shopB, OrderStatus.DELIVERED, "999", "0", "999", now);

        HttpResponse<String> response = get(null, vendorAToken);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode data = mapper.readTree(response.body()).path("data");
        assertThat(data.path("dateBasis").asText()).isEqualTo("ORDER_CREATED_AT");
        assertThat(data.path("totalOrders").asLong()).isEqualTo(6);
        assertThat(data.path("ordersByStatus").path("PENDING").asLong()).isEqualTo(1);
        assertThat(data.path("ordersByStatus").path("CONFIRMED").asLong()).isEqualTo(1);
        assertThat(data.path("ordersByStatus").path("SHIPPED").asLong()).isEqualTo(1);
        assertThat(data.path("ordersByStatus").path("DELIVERED").asLong()).isEqualTo(2);
        assertThat(data.path("ordersByStatus").path("CANCELLED").asLong()).isEqualTo(1);
        assertThat(decimal(data, "fulfilledGrossOrderValue")).isEqualByComparingTo("300");
        assertThat(decimal(data, "voucherDiscountAmount")).isEqualByComparingTo("30");
        assertThat(decimal(data, "fulfilledOrderValue")).isEqualByComparingTo("270");
        assertThat(decimal(data, "averageFulfilledOrderValue")).isEqualByComparingTo("135");
        assertThat(data.path("dailyChart").size()).isEqualTo(1);
        assertThat(decimal(data.path("dailyChart").get(0), "fulfilledOrderValue")).isEqualByComparingTo("270");
    }

    @Test
    void maliciousShopIdCannotChangeScopeAndOtherVendorSeesOnlyOwnData() throws Exception {
        create(shopA, OrderStatus.DELIVERED, "100", "0", "100", LocalDateTime.now());
        create(shopB, OrderStatus.DELIVERED, "250", "0", "250", LocalDateTime.now());

        JsonNode vendorA = mapper.readTree(get("?shopId=" + shopB, vendorAToken).body()).path("data");
        JsonNode vendorB = mapper.readTree(get("?shopId=" + shopA, vendorBToken).body()).path("data");
        assertThat(decimal(vendorA, "fulfilledOrderValue")).isEqualByComparingTo("100");
        assertThat(decimal(vendorB, "fulfilledOrderValue")).isEqualByComparingTo("250");
    }

    @Test
    void securityNoShopAndBannedShopRulesAreEnforced() throws Exception {
        create(shopA, OrderStatus.DELIVERED, "100", "0", "100", LocalDateTime.now());
        assertThat(get(null, null).statusCode()).isIn(401, 403);
        assertThat(get(null, buyerToken).statusCode()).isEqualTo(403);
        assertThat(get(null, adminToken).statusCode()).isEqualTo(403);

        HttpResponse<String> noShop = get(null, vendorWithoutShopToken);
        assertThat(noShop.statusCode()).isEqualTo(404);
        assertThat(mapper.readTree(noShop.body()).path("errorCode").asText()).isEqualTo("SHOP_NOT_FOUND");

        Shop shop = shops.findById(shopA).orElseThrow();
        shop.setStatus(ShopStatus.BANNED);
        shops.save(shop);
        HttpResponse<String> banned = get(null, vendorAToken);
        assertThat(banned.statusCode()).isEqualTo(200);
        assertThat(decimal(mapper.readTree(banned.body()).path("data"), "fulfilledOrderValue")).isEqualByComparingTo("100");
    }

    @Test
    void datesAreInclusiveAndInvalidRangeUsesStandardErrorEnvelope() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDateTime timestamp = LocalDateTime.now().withNano(0);
        create(shopA, OrderStatus.DELIVERED, "10", "0", "10", timestamp);
        create(shopA, OrderStatus.DELIVERED, "20", "0", "20", timestamp.plusSeconds(1));

        JsonNode exactDay = mapper.readTree(get("?startDate=" + date + "&endDate=" + date, vendorAToken).body()).path("data");
        assertThat(exactDay.path("totalOrders").asLong()).isEqualTo(2);
        assertThat(decimal(exactDay, "fulfilledOrderValue")).isEqualByComparingTo("30");

        HttpResponse<String> invalid = get("?startDate=" + date.plusDays(1) + "&endDate=" + date, vendorAToken);
        assertThat(invalid.statusCode()).isEqualTo(400);
        JsonNode error = mapper.readTree(invalid.body());
        assertThat(error.path("success").asBoolean()).isFalse();
        assertThat(error.path("errorCode").asText()).isEqualTo("INVALID_INPUT");
        assertThat(error.path("data").isNull()).isTrue();
    }

    @Test
    void emptyShopReturnsZeroMetricsAndEmptyChart() throws Exception {
        JsonNode data = mapper.readTree(get(null, vendorAToken).body()).path("data");
        assertThat(data.path("totalOrders").asLong()).isZero();
        assertThat(data.path("deliveredOrderCount").asLong()).isZero();
        assertThat(decimal(data, "fulfilledGrossOrderValue")).isEqualByComparingTo("0");
        assertThat(decimal(data, "voucherDiscountAmount")).isEqualByComparingTo("0");
        assertThat(decimal(data, "fulfilledOrderValue")).isEqualByComparingTo("0");
        assertThat(decimal(data, "averageFulfilledOrderValue")).isEqualByComparingTo("0");
        assertThat(data.path("dailyChart").size()).isZero();
    }

    private Role role(String name) {
        return roles.findByName(name).orElseGet(() -> roles.save(Role.builder().name(name).build()));
    }

    private User user(String emailPrefix, Set<Role> roleSet) {
        return users.save(User.builder().email(emailPrefix + "@test.local").fullName("Analytics Test")
                .passwordHash("hashed").status("ACTIVE").roles(roleSet).build());
    }

    private Order create(Long shopId, OrderStatus status, String original, String discount, String total, LocalDateTime createdAt) {
        Order order = orders.save(Order.builder().userId(buyerId).shopId(shopId).status(status)
                .paymentMethod("COD").paymentGroupId("analytics-" + UUID.randomUUID())
                .originalTotalAmount(new BigDecimal(original)).discountAmount(new BigDecimal(discount))
                .totalAmount(new BigDecimal(total)).shippingAddress("Test address").build());
        // Use a DATETIME literal so this fixture does not introduce a JDBC timezone
        // conversion different from the LocalDateTime API contract under test.
        jdbc.update("UPDATE orders SET created_at = CAST(? AS DATETIME) WHERE id = ?",
                createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), order.getId());
        return order;
    }

    private HttpResponse<String> get(String query, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/vendor/analytics" + (query == null ? "" : query))).GET();
        if (token != null) request.header("Authorization", "Bearer " + token);
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private BigDecimal decimal(JsonNode data, String field) {
        return new BigDecimal(data.path(field).asText());
    }
}
