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
class RelatedProductsIntegrationTest {
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
    private Long secondActiveShopId;
    private Long bannedShopId;
    private Category category;
    private Category otherCategory;

    @BeforeEach
    void setUp() {
        String database = jdbc.queryForObject("SELECT DATABASE()", String.class);
        assertNotNull(database);
        assertTrue(database.startsWith("ecommerce_"), "Must run against an isolated test schema, got: " + database);

        prefix = "p202-" + UUID.randomUUID().toString().substring(0, 8);
        activeShopId = shop(ShopStatus.ACTIVE, "active-a");
        secondActiveShopId = shop(ShopStatus.ACTIVE, "active-b");
        bannedShopId = shop(ShopStatus.BANNED, "banned");
        category = categories.save(Category.builder().name(prefix + "-category").build());
        otherCategory = categories.save(Category.builder().name(prefix + "-other-category").build());
    }

    @Test
    void publicEndpointReturnsExactCategoryCrossShopCandidatesInDescendingIdOrder() throws Exception {
        Product seed = product("seed", category, activeShopId, ProductStatus.ACTIVE);
        Product lower = product("related-lower", category, activeShopId, ProductStatus.ACTIVE);
        Product higher = product("related-higher", category, secondActiveShopId, ProductStatus.ACTIVE);
        product("other-category", otherCategory, activeShopId, ProductStatus.ACTIVE);
        product("hidden", category, activeShopId, ProductStatus.HIDDEN);
        product("banned", category, bannedShopId, ProductStatus.ACTIVE);

        HttpResponse<String> response = get(seed.getId(), "?limit=8");
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode body = mapper.readTree(response.body());
        assertThat(body.path("success").asBoolean()).isTrue();

        List<Long> ids = ids(body.path("data"));
        assertThat(ids).containsExactly(higher.getId(), lower.getId());
        assertThat(ids).doesNotContain(seed.getId());
        assertThat(ids).doesNotContain(productIdsByName("other-category", "hidden", "banned").toArray(Long[]::new));
    }

    @Test
    void limitDefaultsToEightAndClampsLargeValuesWithoutFallback() throws Exception {
        Product seed = product("seed", category, activeShopId, ProductStatus.ACTIVE);
        for (int i = 0; i < 9; i++) {
            product("related-" + i, category, i % 2 == 0 ? activeShopId : secondActiveShopId, ProductStatus.ACTIVE);
        }

        JsonNode defaultData = mapper.readTree(get(seed.getId(), "").body()).path("data");
        JsonNode clampedData = mapper.readTree(get(seed.getId(), "?limit=999").body()).path("data");
        assertThat(defaultData.size()).isEqualTo(8);
        assertThat(clampedData.size()).isEqualTo(8);
        assertThat(ids(defaultData)).containsExactlyElementsOf(ids(clampedData));

        Product isolated = product("isolated", otherCategory, activeShopId, ProductStatus.ACTIVE);
        JsonNode empty = mapper.readTree(get(isolated.getId(), "").body()).path("data");
        assertThat(empty).isEmpty();
    }

    @Test
    void nonPositiveAndMalformedLimitsUseStandardInvalidInputEnvelope() throws Exception {
        Product seed = product("seed", category, activeShopId, ProductStatus.ACTIVE);

        for (String query : List.of("?limit=0", "?limit=-1", "?limit=not-a-number")) {
            HttpResponse<String> response = get(seed.getId(), query);
            assertThat(response.statusCode()).isEqualTo(400);
            JsonNode body = mapper.readTree(response.body());
            assertThat(body.path("success").asBoolean()).isFalse();
            assertThat(body.path("errorCode").asText()).isEqualTo("INVALID_INPUT");
            assertThat(body.path("data").isNull()).isTrue();
        }
    }

    @Test
    void missingHiddenAndBannedSeedsAreIndistinguishableNotFound() throws Exception {
        Product hidden = product("hidden-seed", category, activeShopId, ProductStatus.HIDDEN);
        Product banned = product("banned-seed", category, bannedShopId, ProductStatus.ACTIVE);

        for (Long seedId : List.of(hidden.getId(), banned.getId(), Long.MAX_VALUE)) {
            HttpResponse<String> response = get(seedId, "");
            assertThat(response.statusCode()).isEqualTo(404);
            JsonNode body = mapper.readTree(response.body());
            assertThat(body.path("success").asBoolean()).isFalse();
            assertThat(body.path("errorCode").asText()).isEqualTo("PRODUCT_NOT_FOUND");
            assertThat(body.path("data").isNull()).isTrue();
        }
    }

    @Test
    void resultContainsEachProductOnceEvenWhenProductsHaveManyVariants() throws Exception {
        Product seed = product("seed", category, activeShopId, ProductStatus.ACTIVE);
        Product first = product("first", category, activeShopId, ProductStatus.ACTIVE);
        Product second = product("second", category, secondActiveShopId, ProductStatus.ACTIVE);
        variant(first, "a");
        variant(first, "b");
        variant(second, "c");

        JsonNode data = mapper.readTree(get(seed.getId(), "").body()).path("data");
        List<Long> ids = ids(data);
        assertThat(ids).containsExactly(second.getId(), first.getId());
        assertThat(new HashSet<>(ids)).hasSize(ids.size());
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

    private void variant(Product product, String suffix) {
        variants.save(ProductVariant.builder()
                .product(product)
                .sku(prefix + "-sku-" + suffix)
                .price(BigDecimal.TEN)
                .stockQuantity(10)
                .status(ProductStatus.ACTIVE)
                .build());
    }

    private HttpResponse<String> get(Long productId, String query) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(
                "http://127.0.0.1:" + port + "/api/v1/products/" + productId + "/related" + query))
                .GET().build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<Long> ids(JsonNode items) {
        java.util.ArrayList<Long> ids = new java.util.ArrayList<>();
        items.forEach(item -> ids.add(item.path("id").asLong()));
        return ids;
    }

    private List<Long> productIdsByName(String... suffixes) {
        return java.util.Arrays.stream(suffixes)
                .map(suffix -> products.findByNameContainingIgnoreCase(prefix + "-" + suffix)
                        .stream().findFirst().orElseThrow().getId())
                .toList();
    }
}
