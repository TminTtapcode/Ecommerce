package com.tmt.ecommerce.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmt.ecommerce.product.entity.Category;
import com.tmt.ecommerce.product.entity.Product;
import com.tmt.ecommerce.product.entity.ProductVariant;
import com.tmt.ecommerce.product.enums.ProductStatus;
import com.tmt.ecommerce.product.repository.CategoryRepository;
import com.tmt.ecommerce.product.repository.ProductRepository;
import com.tmt.ecommerce.product.repository.ProductVariantRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendedProductsIntegrationTest {
    @LocalServerPort int port;
    @Autowired ProductRepository products;
    @Autowired ProductVariantRepository variants;
    @Autowired CategoryRepository categories;
    @Autowired ShopRepository shops;
    @Autowired JdbcTemplate jdbc;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();
    private String prefix;
    private Long activeShopId;
    private Long bannedShopId;
    private Category category;

    @BeforeEach
    void setUp() {
        String database = jdbc.queryForObject("SELECT DATABASE()", String.class);
        assertNotNull(database);
        assertTrue(database.startsWith("ecommerce_"), "Must run against an isolated test schema, got: " + database);

        prefix = "p302-" + UUID.randomUUID().toString().substring(0, 8);
        activeShopId = shop(ShopStatus.ACTIVE, "active-a");
        bannedShopId = shop(ShopStatus.BANNED, "banned");
        category = categories.save(Category.builder().name(prefix + "-category").build());
    }

    @Test
    void publicEndpointReturnsRecommendations() throws Exception {
        Product newest = product("newest", category, activeShopId, ProductStatus.ACTIVE);
        Product hidden = product("hidden", category, activeShopId, ProductStatus.HIDDEN);
        Product banned = product("banned", category, bannedShopId, ProductStatus.ACTIVE);

        HttpResponse<String> response = get("?limit=5");
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode body = mapper.readTree(response.body());
        assertThat(body.path("success").asBoolean()).isTrue();

        List<Long> ids = ids(body.path("data").path("content"));

        assertThat(ids).contains(newest.getId());
        assertThat(ids).doesNotContain(hidden.getId());
        assertThat(ids).doesNotContain(banned.getId());
    }

    @Test
    void limitDefaultsToFifteenAndClampsLargeValues() throws Exception {
        for (int i = 0; i < 20; i++) {
            product("recommend-" + i, category, activeShopId, ProductStatus.ACTIVE);
        }

        JsonNode defaultData = mapper.readTree(get("").body()).path("data").path("content");
        JsonNode clampedData = mapper.readTree(get("?limit=999").body()).path("data").path("content");

        assertThat(defaultData.size()).isLessThanOrEqualTo(15);
        assertThat(clampedData.size()).isLessThanOrEqualTo(50);
    }

    private Long shop(ShopStatus status, String suffix) {
        return shops.save(Shop.builder()
                .name(prefix + "-" + suffix)
                .userId(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .status(status)
                .build()).getId();
    }

    private Product product(String suffix, Category productCategory, Long shopId, ProductStatus status) {
        return products.save(Product.builder()
                .name(prefix + "-" + suffix)
                .description("Recommendation fixture")
                .price(BigDecimal.TEN)
                .stockQuantity(10)
                .shopId(shopId)
                .status(status)
                .category(productCategory)
                .build());
    }

    private HttpResponse<String> get(String query) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(
                "http://127.0.0.1:" + port + "/api/v1/products/recommendations" + query))
                .GET().build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<Long> ids(JsonNode items) {
        java.util.ArrayList<Long> ids = new java.util.ArrayList<>();
        items.forEach(item -> ids.add(item.path("id").asLong()));
        return ids;
    }
}
