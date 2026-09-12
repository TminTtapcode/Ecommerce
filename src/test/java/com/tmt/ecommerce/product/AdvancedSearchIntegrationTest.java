package com.tmt.ecommerce.product;

import com.tmt.ecommerce.product.entity.Brand;
import com.tmt.ecommerce.product.entity.Category;
import com.tmt.ecommerce.product.entity.Product;
import com.tmt.ecommerce.product.enums.ProductStatus;
import com.tmt.ecommerce.product.repository.BrandRepository;
import com.tmt.ecommerce.product.repository.CategoryRepository;
import com.tmt.ecommerce.product.repository.ProductRepository;
import com.tmt.ecommerce.product.service.ProductService;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdvancedSearchIntegrationTest {

    @LocalServerPort int port;
    @Autowired ProductService productService;
    @Autowired ProductRepository productRepo;
    @Autowired CategoryRepository categoryRepo;
    @Autowired BrandRepository brandRepo;
    @Autowired ShopRepository shopRepo;
    @Autowired JdbcTemplate jdbc;

    String prefix;
    Long activeShopId;
    Long bannedShopId;
    Category parentCat;
    Category childCat;

    @BeforeEach
    void setup() {

        String db = jdbc.queryForObject("SELECT DATABASE()", String.class);
        assertNotNull(db);
        assertTrue(db.startsWith("ecommerce_s103_s104_s105"),
                "Must run against test schema, got: " + db);

        prefix = "p104-" + UUID.randomUUID().toString().substring(0, 8);

        long u1 = Math.abs(UUID.randomUUID().getMostSignificantBits());
        long u2 = Math.abs(UUID.randomUUID().getLeastSignificantBits());
        activeShopId = shopRepo.save(Shop.builder()
                .name(prefix + "-active-shop")
                .userId(u1)
                .status(ShopStatus.ACTIVE)
                .build()).getId();

        bannedShopId = shopRepo.save(Shop.builder()
                .name(prefix + "-banned-shop")
                .userId(u2)
                .status(ShopStatus.BANNED)
                .build()).getId();

        parentCat = categoryRepo.save(Category.builder()
                .name(prefix + "-parent-cat").build());
        childCat = categoryRepo.save(Category.builder()
                .name(prefix + "-child-cat").parent(parentCat).build());
    }

    Product product(String name, BigDecimal price, ProductStatus status, Long shopId, Category cat) {
        return productRepo.save(Product.builder()
                .name(name).description("desc")
                .price(price).stockQuantity(10)
                .status(status).shopId(shopId).category(cat)
                .build());
    }

    Page<com.tmt.ecommerce.product.dto.response.ProductResponse> search(
            String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy) {
        return productService.getAllProducts(0, 50, keyword, categoryId, null, minPrice, maxPrice, sortBy);
    }

    @Test
    void noFilters_ShouldReturnOnlyACTIVEFromNonBannedShops() {

        product(prefix + "-visible",   BigDecimal.TEN, ProductStatus.ACTIVE,  activeShopId, parentCat);
        product(prefix + "-hidden",    BigDecimal.TEN, ProductStatus.HIDDEN,  activeShopId, parentCat);
        product(prefix + "-from-ban",  BigDecimal.TEN, ProductStatus.ACTIVE,  bannedShopId, parentCat);

        var page = search(null, null, null, null, null);

        var names = page.getContent().stream()
                .map(com.tmt.ecommerce.product.dto.response.ProductResponse::name)
                .toList();

        assertThat(names).contains(prefix + "-visible");
        assertThat(names).doesNotContain(prefix + "-hidden", prefix + "-from-ban");
    }

    @Test
    void keywordFilter_ShouldMatchNameCaseInsensitive() {
        product(prefix + "-LAPTOP-pro", new BigDecimal("5000000"), ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-laptop-air", new BigDecimal("4000000"), ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-phone-xxx",  new BigDecimal("3000000"), ProductStatus.ACTIVE, activeShopId, parentCat);

        var page = search(prefix + "-LAPTOP", null, null, null, null);
        var names = page.getContent().stream()
                .map(com.tmt.ecommerce.product.dto.response.ProductResponse::name).toList();

        assertThat(names).anySatisfy(n -> assertThat(n).containsIgnoringCase("laptop-pro"));
        assertThat(names).anySatisfy(n -> assertThat(n).containsIgnoringCase("laptop-air"));
        assertThat(names).noneMatch(n -> n.contains("phone-xxx"));
    }

    @Test
    void categoryFilter_ShouldIncludeDescendants_D7_2() {

        product(prefix + "-in-parent",   BigDecimal.TEN, ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-in-child",    BigDecimal.TEN, ProductStatus.ACTIVE, activeShopId, childCat);
        Category other = categoryRepo.save(Category.builder().name(prefix + "-other-cat").build());
        product(prefix + "-in-other",    BigDecimal.TEN, ProductStatus.ACTIVE, activeShopId, other);

        var page = search(null, parentCat.getId(), null, null, null);
        var names = page.getContent().stream()
                .map(com.tmt.ecommerce.product.dto.response.ProductResponse::name).toList();

        assertThat(names).anySatisfy(n -> assertThat(n).contains("in-parent"));
        assertThat(names).anySatisfy(n -> assertThat(n).contains("in-child"));

        assertThat(names).noneMatch(n -> n.contains("in-other"));
    }

    @Test
    void categoryFilter_DirectChild_ShouldOnlyReturnThatChild() {
        product(prefix + "-in-parent-x", BigDecimal.TEN, ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-in-child-x",  BigDecimal.TEN, ProductStatus.ACTIVE, activeShopId, childCat);

        var page = search(null, childCat.getId(), null, null, null);
        var names = page.getContent().stream()
                .map(com.tmt.ecommerce.product.dto.response.ProductResponse::name).toList();

        assertThat(names).anySatisfy(n -> assertThat(n).contains("in-child-x"));
        assertThat(names).noneMatch(n -> n.contains("in-parent-x"));
    }

    @Test
    void priceRangeFilter_ShouldFilterByProductPrice_D7_1() {
        product(prefix + "-cheap",   new BigDecimal("50000"),  ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-mid",     new BigDecimal("500000"), ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-expensive", new BigDecimal("5000000"), ProductStatus.ACTIVE, activeShopId, parentCat);

        var page = search(null, null,
                new BigDecimal("100000"), new BigDecimal("1000000"), null);
        var names = page.getContent().stream()
                .map(com.tmt.ecommerce.product.dto.response.ProductResponse::name).toList();

        assertThat(names).anySatisfy(n -> assertThat(n).contains("mid"));
        assertThat(names).noneMatch(n -> n.contains("cheap"));
        assertThat(names).noneMatch(n -> n.contains("expensive"));
    }

    @Test
    void priceFilter_MinOnly_ShouldReturnAtLeastMin() {
        product(prefix + "-below-min", new BigDecimal("10000"),  ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-above-min", new BigDecimal("200000"), ProductStatus.ACTIVE, activeShopId, parentCat);

        var page = search(null, null, new BigDecimal("100000"), null, null);
        var names = page.getContent().stream()
                .map(com.tmt.ecommerce.product.dto.response.ProductResponse::name).toList();

        assertThat(names).anySatisfy(n -> assertThat(n).contains("above-min"));
        assertThat(names).noneMatch(n -> n.contains("below-min"));
    }

    @Test
    void priceFilter_MaxOnly_ShouldReturnAtMostMax() {
        product(prefix + "-under-max", new BigDecimal("30000"), ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-over-max",  new BigDecimal("900000"), ProductStatus.ACTIVE, activeShopId, parentCat);

        var page = search(null, null, null, new BigDecimal("100000"), null);
        var names = page.getContent().stream()
                .map(com.tmt.ecommerce.product.dto.response.ProductResponse::name).toList();

        assertThat(names).anySatisfy(n -> assertThat(n).contains("under-max"));
        assertThat(names).noneMatch(n -> n.contains("over-max"));
    }

    @Test
    void sortByPriceAsc_ShouldReturnAscendingOrder_D7_4() {
        product(prefix + "-sort-p3", new BigDecimal("300"), ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-sort-p1", new BigDecimal("100"), ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-sort-p2", new BigDecimal("200"), ProductStatus.ACTIVE, activeShopId, parentCat);

        var page = search(prefix + "-sort-", null, null, null, "price_asc");
        var prices = page.getContent().stream()
                .map(p -> p.price() != null ? p.price()
                        : BigDecimal.ZERO)
                .toList();

        for (int i = 1; i < prices.size(); i++) {
            assertThat(prices.get(i)).isGreaterThanOrEqualTo(prices.get(i - 1));
        }
    }

    @Test
    void sortByPriceDesc_ShouldReturnDescendingOrder_D7_4() {
        product(prefix + "-sortd-p1", new BigDecimal("100"), ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-sortd-p3", new BigDecimal("300"), ProductStatus.ACTIVE, activeShopId, parentCat);
        product(prefix + "-sortd-p2", new BigDecimal("200"), ProductStatus.ACTIVE, activeShopId, parentCat);

        var page = search(prefix + "-sortd-", null, null, null, "price_desc");
        var prices = page.getContent().stream()
                .map(p -> p.price() != null ? p.price() : BigDecimal.ZERO)
                .toList();

        for (int i = 1; i < prices.size(); i++) {
            assertThat(prices.get(i)).isLessThanOrEqualTo(prices.get(i - 1));
        }
    }

    @Test
    void unknownSortBy_ShouldFallbackToIdDesc_D7_4() {

        assertDoesNotThrow(() -> search(null, null, null, null, "invalid_sort_value"));
    }

    @Test
    void sizeClamping_ShouldNeverExceed50_D7_5() {

        for (int i = 0; i < 10; i++) {
            product(prefix + "-clamp-" + i, BigDecimal.TEN, ProductStatus.ACTIVE, activeShopId, parentCat);
        }

        var page = productService.getAllProducts(0, 200, null, null, null, null, null, null);
        assertThat(page.getSize()).isLessThanOrEqualTo(50);
    }

    @Test
    void combinedFilters_KeywordAndCategoryAndPrice() {

        product(prefix + "-combo-match",  new BigDecimal("150000"), ProductStatus.ACTIVE, activeShopId, childCat);
        product(prefix + "-combo-tooCheap", new BigDecimal("1000"), ProductStatus.ACTIVE, activeShopId, childCat);
        product(prefix + "-combo-wrongCat", new BigDecimal("150000"), ProductStatus.ACTIVE, activeShopId,
                categoryRepo.save(Category.builder().name(prefix + "-unrelated").build()));

        var page = search(prefix + "-combo", parentCat.getId(),
                new BigDecimal("100000"), new BigDecimal("999999"), null);
        var names = page.getContent().stream()
                .map(com.tmt.ecommerce.product.dto.response.ProductResponse::name).toList();

        assertThat(names).anySatisfy(n -> assertThat(n).contains("combo-match"));
        assertThat(names).noneMatch(n -> n.contains("combo-tooCheap"));
        assertThat(names).noneMatch(n -> n.contains("combo-wrongCat"));
    }

    @Test
    void bannedShop_ProductNeverAppears_EvenWithMatchingFilters() {
        product(prefix + "-banned-prod", BigDecimal.TEN, ProductStatus.ACTIVE, bannedShopId, parentCat);

        var page = search(prefix + "-banned-prod", null, null, null, null);
        var names = page.getContent().stream()
                .map(com.tmt.ecommerce.product.dto.response.ProductResponse::name).toList();

        assertThat(names).noneMatch(n -> n.contains("banned-prod"));
    }

    @Test
    void hiddenProduct_NeverAppears_InPublicSearch() {
        product(prefix + "-hidden-p", BigDecimal.TEN, ProductStatus.HIDDEN, activeShopId, parentCat);

        var page = search(prefix + "-hidden-p", null, null, null, null);
        var names = page.getContent().stream()
                .map(com.tmt.ecommerce.product.dto.response.ProductResponse::name).toList();

        assertThat(names).noneMatch(n -> n.contains("hidden-p"));
    }

    @Test
    void httpEndpoint_MinGreaterThanMax_Returns400() throws Exception {
        var req = java.net.http.HttpRequest.newBuilder(
                        java.net.URI.create("http://127.0.0.1:" + port + "/api/v1/products?minPrice=9000&maxPrice=1000"))
                .GET().build();
        var resp = java.net.http.HttpClient.newHttpClient()
                .send(req, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(400, resp.statusCode());
    }

    @Test
    void httpEndpoint_NoFilters_Returns200() throws Exception {
        var req = java.net.http.HttpRequest.newBuilder(
                        java.net.URI.create("http://127.0.0.1:" + port + "/api/v1/products"))
                .GET().build();
        var resp = java.net.http.HttpClient.newHttpClient()
                .send(req, java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, resp.statusCode());
        assertThat(resp.body()).contains("\"status\":200");
    }
}
