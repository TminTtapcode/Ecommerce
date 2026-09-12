package com.tmt.ecommerce;

import com.tmt.ecommerce.cart.dto.request.CartItemRequest;
import com.tmt.ecommerce.cart.service.CartService;
import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.service.OrderService;
import com.tmt.ecommerce.product.entity.Category;
import com.tmt.ecommerce.product.entity.Product;
import com.tmt.ecommerce.product.entity.ProductVariant;
import com.tmt.ecommerce.product.repository.CategoryRepository;
import com.tmt.ecommerce.product.repository.ProductRepository;
import com.tmt.ecommerce.product.repository.ProductVariantRepository;
import com.tmt.ecommerce.review.service.ReviewService;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NPlus1AuditTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private com.tmt.ecommerce.product.service.ProductService productService;

    @Autowired
    private com.tmt.ecommerce.order.repository.OrderRepository orderRepository;

    @Autowired
    private com.tmt.ecommerce.review.repository.ReviewRepository reviewRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired private com.tmt.ecommerce.shop.repository.ShopRepository shops;
    private Long shop1, shop2;
    private String listingPrefix;
    private Long userId = 1001L;
    private Long categoryId;
    private Long product1Id, product2Id;
    private Long variant1Id, variant2Id;

    @BeforeEach
    public void setup() {
        listingPrefix = "query-" + java.util.UUID.randomUUID();
        shop1 = shops.save(com.tmt.ecommerce.shop.entity.Shop.builder().userId(System.nanoTime())
                .name(listingPrefix + "-one").status(com.tmt.ecommerce.shop.enums.ShopStatus.ACTIVE).build()).getId();
        shop2 = shops.save(com.tmt.ecommerce.shop.entity.Shop.builder().userId(System.nanoTime())
                .name(listingPrefix + "-two").status(com.tmt.ecommerce.shop.enums.ShopStatus.ACTIVE).build()).getId();
        Category cat = new Category();
        cat.setName("Test Cat " + java.util.UUID.randomUUID().toString());
        cat = categoryRepository.save(cat);
        categoryId = cat.getId();

        Product p1 = new Product();
        p1.setName(listingPrefix + " Product 1");
        p1.setShopId(shop1);
        p1.setCategory(cat);
        p1.setStatus(com.tmt.ecommerce.product.enums.ProductStatus.ACTIVE);
        p1.setPrice(BigDecimal.valueOf(100));
        p1.setStockQuantity(10);
        p1 = productRepository.save(p1);
        product1Id = p1.getId();

        ProductVariant pv1 = new ProductVariant();
        pv1.setProduct(p1);
        pv1.setPrice(BigDecimal.valueOf(100));
        pv1.setStockQuantity(10);
        pv1.setSku("SKU1-" + java.util.UUID.randomUUID().toString());
        pv1.setStatus(com.tmt.ecommerce.product.enums.ProductStatus.ACTIVE);
        pv1 = productVariantRepository.save(pv1);
        variant1Id = pv1.getId();

        Product p2 = new Product();
        p2.setName(listingPrefix + " Product 2");
        p2.setShopId(shop2);
        p2.setCategory(cat);
        p2.setStatus(com.tmt.ecommerce.product.enums.ProductStatus.ACTIVE);
        p2.setPrice(BigDecimal.valueOf(200));
        p2.setStockQuantity(20);
        p2 = productRepository.save(p2);
        product2Id = p2.getId();

        ProductVariant pv2 = new ProductVariant();
        pv2.setProduct(p2);
        pv2.setPrice(BigDecimal.valueOf(200));
        pv2.setStockQuantity(20);
        pv2.setSku("SKU2-" + java.util.UUID.randomUUID().toString());
        pv2.setStatus(com.tmt.ecommerce.product.enums.ProductStatus.ACTIVE);
        pv2 = productVariantRepository.save(pv2);
        variant2Id = pv2.getId();

        userId = (long) (Math.random() * 100000);
        CartItemRequest req1 = new CartItemRequest();
        req1.setProductVariantId(variant1Id);
        req1.setQuantity(1);
        cartService.addToCart(userId, req1);

        CartItemRequest req2 = new CartItemRequest();
        req2.setProductVariantId(variant2Id);
        req2.setQuantity(2);
        cartService.addToCart(userId, req2);
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testCartNPlus1() {
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        cartService.getCart(userId);

        System.out.println("====== GET CART QUERY COUNT: " + stats.getPrepareStatementCount() + " ======");
    }

    @Test
    public void testOrderCheckoutNPlus1() {
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        CheckoutRequest req = new CheckoutRequest("Address", "COD", null, null);
        orderService.checkout(userId, req);

        System.out.println("====== CHECKOUT QUERY COUNT: " + stats.getPrepareStatementCount() + " ======");
    }

    @Test
    public void testProductListingBatchFetching() {
        // Seed 3 more products with variants and images
        Category cat = categoryRepository.findById(categoryId).orElseThrow();
        for (int i = 0; i < 3; i++) {
            Product p = Product.builder()
                    .name(listingPrefix + " Listing Product " + i)
                    .shopId(shop1)
                    .category(cat)
                    .status(com.tmt.ecommerce.product.enums.ProductStatus.ACTIVE)
                    .price(BigDecimal.valueOf(150 + i * 10))
                    .stockQuantity(50)
                    .build();
            p.setImages(new java.util.ArrayList<>());
            p = productRepository.save(p);

            ProductVariant pv = ProductVariant.builder()
                    .product(p)
                    .sku("LIST-SKU-" + i + "-" + java.util.UUID.randomUUID())
                    .price(BigDecimal.valueOf(150))
                    .stockQuantity(50)
                    .status(com.tmt.ecommerce.product.enums.ProductStatus.ACTIVE)
                    .build();
            productVariantRepository.save(pv);

            com.tmt.ecommerce.product.entity.ProductImage img = com.tmt.ecommerce.product.entity.ProductImage.builder()
                    .product(p)
                    .imageUrl("https://example.com/img" + i + ".png")
                    .isThumbnail(true)
                    .sortOrder(0)
                    .build();
            p.getImages().add(img);
            productRepository.save(p);
        }

        entityManager.flush();
        entityManager.clear();

        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        org.springframework.data.domain.Page<com.tmt.ecommerce.product.dto.response.ProductResponse> productPage = 
                productService.getAllProducts(0, 10, listingPrefix);

        // Fully access child collections and joined properties
        for (com.tmt.ecommerce.product.dto.response.ProductResponse prod : productPage.getContent()) {
            org.junit.jupiter.api.Assertions.assertNotNull(prod.categoryName());
            prod.variants().size();
            prod.imageResponses().size();
        }

        long queryCount = stats.getPrepareStatementCount();
        System.out.println("====== PRODUCT LISTING QUERY COUNT: " + queryCount + " ======");
        // Expected bounded queries: 1 count + 1 product/category join + 1 batch variants + 1 batch images = 4
        org.junit.jupiter.api.Assertions.assertTrue(queryCount <= 6, 
                "Product listing queries should be bounded by batch fetching, but was " + queryCount);
    }

    @Test
    public void testOrderListingBatchFetching() {
        Long orderUserId = userId;
        // Create 4 orders for this user, each with 2 items
        for (int i = 0; i < 4; i++) {
            com.tmt.ecommerce.order.entity.Order order = com.tmt.ecommerce.order.entity.Order.builder()
                    .userId(orderUserId)
                    .shopId(2001L)
                    .paymentGroupId(java.util.UUID.randomUUID().toString())
                    .status(com.tmt.ecommerce.order.entity.OrderStatus.CONFIRMED)
                    .totalAmount(BigDecimal.valueOf(300))
                    .originalTotalAmount(BigDecimal.valueOf(300))
                    .discountAmount(BigDecimal.ZERO)
                    .shippingAddress("Address " + i)
                    .paymentMethod("COD")
                    .build();

            com.tmt.ecommerce.order.entity.OrderItem item1 = com.tmt.ecommerce.order.entity.OrderItem.builder()
                    .order(order)
                    .productVariantId(variant1Id)
                    .productName("Product 1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100))
                    .subTotal(BigDecimal.valueOf(100))
                    .build();

            com.tmt.ecommerce.order.entity.OrderItem item2 = com.tmt.ecommerce.order.entity.OrderItem.builder()
                    .order(order)
                    .productVariantId(variant2Id)
                    .productName("Product 2")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(200))
                    .subTotal(BigDecimal.valueOf(200))
                    .build();

            order.addOrderItem(item1);
            order.addOrderItem(item2);
            orderRepository.save(order);
        }

        entityManager.flush();
        entityManager.clear();

        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        org.springframework.data.domain.Page<com.tmt.ecommerce.order.dto.response.OrderResponse> orderPage = 
                orderService.getMyOrders(orderUserId, null, 0, 10);

        for (com.tmt.ecommerce.order.dto.response.OrderResponse ord : orderPage.getContent()) {
            org.junit.jupiter.api.Assertions.assertEquals(2, ord.items().size());
            org.junit.jupiter.api.Assertions.assertNotNull(ord.items().get(0).productId());
        }

        long queryCount = stats.getPrepareStatementCount();
        System.out.println("====== ORDER LISTING QUERY COUNT: " + queryCount + " ======");
        // Expected measured queries: 1 count + 1 orders page + 1 batch items (@BatchSize) + 1 bulk variant-to-product mapping + user full name lookups = 7 queries
        org.junit.jupiter.api.Assertions.assertTrue(queryCount <= 8, 
                "Order listing queries should be bounded by batch size and bulk API, but was " + queryCount);
    }

    @Test
    public void testReviewEligibilityBulkQuery() {
        Long reviewUserId = (long) (Math.random() * 1000000 + 700000);
        
        // Create 3 delivered orders containing variant1 (product1) and 1 order containing variant2 (product2)
        com.tmt.ecommerce.order.entity.Order order1 = com.tmt.ecommerce.order.entity.Order.builder()
                .userId(reviewUserId)
                .shopId(2001L)
                .paymentGroupId(java.util.UUID.randomUUID().toString())
                .status(com.tmt.ecommerce.order.entity.OrderStatus.DELIVERED)
                .totalAmount(BigDecimal.valueOf(100))
                .shippingAddress("Addr 1")
                .paymentMethod("COD")
                .build();
        order1.addOrderItem(com.tmt.ecommerce.order.entity.OrderItem.builder()
                .order(order1).productVariantId(variant1Id).productName("P1").quantity(1).unitPrice(BigDecimal.valueOf(100)).subTotal(BigDecimal.valueOf(100)).build());
        order1 = orderRepository.save(order1);

        com.tmt.ecommerce.order.entity.Order order2 = com.tmt.ecommerce.order.entity.Order.builder()
                .userId(reviewUserId)
                .shopId(2001L)
                .paymentGroupId(java.util.UUID.randomUUID().toString())
                .status(com.tmt.ecommerce.order.entity.OrderStatus.DELIVERED)
                .totalAmount(BigDecimal.valueOf(100))
                .shippingAddress("Addr 2")
                .paymentMethod("COD")
                .build();
        order2.addOrderItem(com.tmt.ecommerce.order.entity.OrderItem.builder()
                .order(order2).productVariantId(variant1Id).productName("P1").quantity(1).unitPrice(BigDecimal.valueOf(100)).subTotal(BigDecimal.valueOf(100)).build());
        order2 = orderRepository.save(order2);

        com.tmt.ecommerce.order.entity.Order order3 = com.tmt.ecommerce.order.entity.Order.builder()
                .userId(reviewUserId)
                .shopId(2002L)
                .paymentGroupId(java.util.UUID.randomUUID().toString())
                .status(com.tmt.ecommerce.order.entity.OrderStatus.DELIVERED)
                .totalAmount(BigDecimal.valueOf(200))
                .shippingAddress("Addr 3")
                .paymentMethod("COD")
                .build();
        order3.addOrderItem(com.tmt.ecommerce.order.entity.OrderItem.builder()
                .order(order3).productVariantId(variant2Id).productName("P2").quantity(1).unitPrice(BigDecimal.valueOf(200)).subTotal(BigDecimal.valueOf(200)).build());
        order3 = orderRepository.save(order3);

        // Add an existing review for order1 and product1
        com.tmt.ecommerce.review.entity.Review review = com.tmt.ecommerce.review.entity.Review.builder()
                .userId(reviewUserId)
                .orderId(order1.getId())
                .productId(product1Id)
                .rating(5)
                .comment("Great product!")
                .build();
        reviewRepository.save(review);

        entityManager.flush();
        entityManager.clear();

        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        List<Long> eligibleOrderIds = reviewService.checkReviewEligibility(reviewUserId, product1Id);

        long queryCount = stats.getPrepareStatementCount();
        System.out.println("====== REVIEW ELIGIBILITY QUERY COUNT: " + queryCount + " ======");
        
        // Assertions: only order2 is eligible (order1 was already reviewed, order3 does not contain product1)
        org.junit.jupiter.api.Assertions.assertEquals(1, eligibleOrderIds.size());
        org.junit.jupiter.api.Assertions.assertEquals(order2.getId(), eligibleOrderIds.get(0));

        // Exact queries: 1 projection + 1 bulk variant-to-product + 1 bulk findReviewedOrderIds = 3 queries
        org.junit.jupiter.api.Assertions.assertEquals(3, queryCount, 
                "Review eligibility should execute exactly 3 queries (projection, bulk variant mapping, bulk review existence check)");
    }
}
