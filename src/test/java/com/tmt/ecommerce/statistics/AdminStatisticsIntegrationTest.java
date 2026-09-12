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
import com.tmt.ecommerce.order.entity.OrderItem;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.product.entity.Category;
import com.tmt.ecommerce.product.entity.Product;
import com.tmt.ecommerce.product.enums.ProductStatus;
import com.tmt.ecommerce.product.repository.CategoryRepository;
import com.tmt.ecommerce.product.repository.ProductRepository;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminStatisticsIntegrationTest {

    @LocalServerPort int port;

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired ShopRepository shopRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired JwtService jwtService;
    @Autowired JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String adminToken;
    private String userToken;
    private String vendorToken;
    private Long testUserId;
    private Long testShopId;

    @BeforeEach
    void setup() {
        String db = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        assertNotNull(db);
        assertTrue(db.contains("ecommerce_"), "Must run against test schema, got: " + db);

        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_USER").build()));
        Role vendorRole = roleRepository.findByName("ROLE_VENDOR").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_VENDOR").build()));

        String suffix = UUID.randomUUID().toString().substring(0, 8);

        User adminUser = userRepository.save(User.builder()
                .email("admin-" + suffix + "@test.com")
                .fullName("Admin Test")
                .passwordHash("hashed")
                .status("ACTIVE")
                .roles(Set.of(adminRole))
                .build());
        adminToken = jwtService.generateToken(new CustomUserDetails(adminUser));

        User normalUser = userRepository.save(User.builder()
                .email("user-" + suffix + "@test.com")
                .fullName("Buyer Test")
                .passwordHash("hashed")
                .status("ACTIVE")
                .roles(Set.of(userRole))
                .build());
        userToken = jwtService.generateToken(new CustomUserDetails(normalUser));
        testUserId = normalUser.getId();

        User vendorUser = userRepository.save(User.builder()
                .email("vendor-" + suffix + "@test.com")
                .fullName("Vendor Test")
                .passwordHash("hashed")
                .status("ACTIVE")
                .roles(Set.of(vendorRole))
                .build());
        vendorToken = jwtService.generateToken(new CustomUserDetails(vendorUser));

        Shop shop = shopRepository.save(Shop.builder()
                .name("Shop-" + suffix)
                .userId(vendorUser.getId())
                .status(ShopStatus.ACTIVE)
                .build());
        testShopId = shop.getId();

        Category category = categoryRepository.save(Category.builder().name("Cat-" + suffix).build());
        productRepository.save(Product.builder()
                .shopId(testShopId)
                .category(category)
                .name("Product-" + suffix)
                .price(new BigDecimal("100.00"))
                .stockQuantity(50)
                .status(ProductStatus.ACTIVE)
                .build());
    }

    private HttpResponse<String> getDashboard(String queryParams, String bearerToken) throws Exception {
        String url = "http://127.0.0.1:" + port + "/api/v1/admin/statistics/dashboard"
                + (queryParams != null ? queryParams : "");
        var reqBuilder = HttpRequest.newBuilder(URI.create(url)).GET();
        if (bearerToken != null) {
            reqBuilder.header("Authorization", "Bearer " + bearerToken);
        }
        return httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void unauthenticatedRequest_Returns401or403() throws Exception {
        HttpResponse<String> res = getDashboard(null, null);
        assertThat(res.statusCode()).isIn(401, 403);
    }

    @Test
    void nonAdminRole_ReturnsForbidden403() throws Exception {
        HttpResponse<String> buyerRes = getDashboard(null, userToken);
        assertThat(buyerRes.statusCode()).isEqualTo(403);

        HttpResponse<String> vendorRes = getDashboard(null, vendorToken);
        assertThat(vendorRes.statusCode()).isEqualTo(403);
    }

    @Test
    void adminAccess_SucceedsWith200AndValidStructure() throws Exception {
        HttpResponse<String> res = getDashboard(null, adminToken);
        assertThat(res.statusCode()).isEqualTo(200);

        JsonNode root = objectMapper.readTree(res.body());
        assertThat(root.get("status").asInt()).isEqualTo(200);
        JsonNode data = root.get("data");
        assertThat(data).isNotNull();

        assertThat(data.has("totalUsers")).isTrue();
        assertThat(data.has("totalShops")).isTrue();
        assertThat(data.has("totalProducts")).isTrue();
        assertThat(data.has("totalOrders")).isTrue();
        assertThat(data.has("ordersByStatus")).isTrue();
        assertThat(data.has("deliveredRevenue")).isTrue();
        assertThat(data.has("averageOrderValue")).isTrue();
        assertThat(data.has("dailyChart")).isTrue();
        assertThat(data.has("recentOrders")).isTrue();

        assertThat(data.get("totalUsers").asLong()).isGreaterThanOrEqualTo(3);
        assertThat(data.get("totalShops").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(data.get("totalProducts").asLong()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void statisticsAccuracy_DeliveredRevenueOnlyAndAOVCalculation() throws Exception {

        createOrder(OrderStatus.DELIVERED, new BigDecimal("100.00"), LocalDateTime.now());
        createOrder(OrderStatus.DELIVERED, new BigDecimal("200.00"), LocalDateTime.now());
        createOrder(OrderStatus.CANCELLED, new BigDecimal("50.00"), LocalDateTime.now());
        createOrder(OrderStatus.PENDING, new BigDecimal("50.00"), LocalDateTime.now());

        HttpResponse<String> res = getDashboard(null, adminToken);
        assertThat(res.statusCode()).isEqualTo(200);

        JsonNode data = objectMapper.readTree(res.body()).get("data");
        JsonNode ordersByStatus = data.get("ordersByStatus");

        assertThat(ordersByStatus.has("DELIVERED")).isTrue();
        assertThat(ordersByStatus.get("DELIVERED").asLong()).isGreaterThanOrEqualTo(2);
        assertThat(ordersByStatus.get("CANCELLED").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(ordersByStatus.get("PENDING").asLong()).isGreaterThanOrEqualTo(1);

        BigDecimal revenue = new BigDecimal(data.get("deliveredRevenue").asText());
        BigDecimal aov = new BigDecimal(data.get("averageOrderValue").asText());
        long deliveredCount = ordersByStatus.get("DELIVERED").asLong();

        assertThat(revenue).isGreaterThanOrEqualTo(new BigDecimal("300.00"));
        if (deliveredCount > 0) {
            BigDecimal expectedAov = revenue.divide(BigDecimal.valueOf(deliveredCount), 2, java.math.RoundingMode.HALF_UP);
            assertThat(aov).isEqualByComparingTo(expectedAov);
        }
    }

    @Test
    void dateRangeFiltering_FiltersOrdersAndKeepsBaselineCounts() throws Exception {

        LocalDate futureStart = LocalDate.now().plusDays(10);
        LocalDate futureEnd = LocalDate.now().plusDays(20);

        HttpResponse<String> res = getDashboard(
                "?startDate=" + futureStart + "&endDate=" + futureEnd, adminToken);
        assertThat(res.statusCode()).isEqualTo(200);

        JsonNode data = objectMapper.readTree(res.body()).get("data");

        assertThat(data.get("totalUsers").asLong()).isGreaterThanOrEqualTo(3);
        assertThat(data.get("totalShops").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(data.get("totalProducts").asLong()).isGreaterThanOrEqualTo(1);

        assertThat(data.get("totalOrders").asLong()).isEqualTo(0);
        assertThat(new BigDecimal(data.get("deliveredRevenue").asText())).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(new BigDecimal(data.get("averageOrderValue").asText())).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(data.get("dailyChart").size()).isEqualTo(0);
    }

    @Test
    void recentOrders_ReturnsLatestOrders() throws Exception {
        createOrder(OrderStatus.DELIVERED, new BigDecimal("123.45"), LocalDateTime.now());

        HttpResponse<String> res = getDashboard(null, adminToken);
        assertThat(res.statusCode()).isEqualTo(200);

        JsonNode data = objectMapper.readTree(res.body()).get("data");
        JsonNode recentOrders = data.get("recentOrders");
        assertThat(recentOrders.isArray()).isTrue();
        assertThat(recentOrders.size()).isGreaterThanOrEqualTo(1);
        assertThat(recentOrders.size()).isLessThanOrEqualTo(10);

        JsonNode first = recentOrders.get(0);
        assertThat(first.has("id")).isTrue();
        assertThat(first.has("status")).isTrue();
        assertThat(first.has("totalAmount")).isTrue();
        assertThat(first.has("createdAt")).isTrue();
    }

    private Order createOrder(OrderStatus status, BigDecimal amount, LocalDateTime createdAt) {
        Order o = Order.builder()
                .userId(testUserId)
                .shopId(testShopId)
                .status(status)
                .paymentMethod("COD")
                .paymentGroupId("p105-" + UUID.randomUUID())
                .totalAmount(amount)
                .shippingAddress("Test Address")
                .build();
        o = orderRepository.save(o);

        if (createdAt != null) {
            jdbcTemplate.update("UPDATE orders SET created_at = ? WHERE id = ?", createdAt, o.getId());
        }
        return o;
    }
}
