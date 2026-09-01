package com.tmt.ecommerce.identity.config;

import com.tmt.ecommerce.cart.entity.Cart;
import com.tmt.ecommerce.cart.entity.CartItem;
import com.tmt.ecommerce.cart.repository.CartRepository;
import com.tmt.ecommerce.identity.entity.Role;
import com.tmt.ecommerce.identity.entity.User;
import com.tmt.ecommerce.identity.repository.RoleRepository;
import com.tmt.ecommerce.identity.repository.UserRepository;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderItem;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.payment.entity.Payment;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.enums.PaymentStatus;
import com.tmt.ecommerce.payment.repository.PaymentRepository;
import com.tmt.ecommerce.product.entity.*;
import com.tmt.ecommerce.product.enums.ProductStatus;
import com.tmt.ecommerce.product.repository.*;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== KIỂM TRA & KHỞI TẠO DỮ LIỆU MẪU DỰ ÁN ===");

        // 1. Roles
        Role roleUser = getOrCreateRole("ROLE_USER");
        Role roleVendor = getOrCreateRole("ROLE_VENDOR");
        Role roleAdmin = getOrCreateRole("ROLE_ADMIN");

        // 2. Users (10 Users)
        if (userRepository.count() > 3) {
            log.info("Dữ liệu hệ thống đã tồn tại. Bỏ qua DataInitializer.");
            return;
        }

        String encodedPassword = passwordEncoder.encode("password123");

        // Admin User (1)
        User admin = createUser("admin@ecommerce.com", "Trần Quản Trị", "0901000001", encodedPassword, Set.of(roleAdmin, roleUser));

        // Vendor Users (3)
        User vendor1 = createUser("vendor1@techstore.com", "Nguyễn Văn Tech", "0902000001", encodedPassword, Set.of(roleVendor, roleUser));
        User vendor2 = createUser("vendor2@fashionhub.com", "Lê Thị Thời Trang", "0902000002", encodedPassword, Set.of(roleVendor, roleUser));
        User vendor3 = createUser("vendor3@giadungxanh.com", "Phạm Văn Gia Dụng", "0902000003", encodedPassword, Set.of(roleVendor, roleUser));

        // Customer Users (6)
        User customer1 = createUser("customer1@gmail.com", "Đặng Minh Triết", "0903000001", encodedPassword, Set.of(roleUser));
        User customer2 = createUser("customer2@gmail.com", "Vũ Hoàng Nam", "0903000002", encodedPassword, Set.of(roleUser));
        User customer3 = createUser("customer3@gmail.com", "Trần Thị Mai", "0903000003", encodedPassword, Set.of(roleUser));
        User customer4 = createUser("customer4@gmail.com", "Ngô Quốc Bảo", "0903000004", encodedPassword, Set.of(roleUser));
        User customer5 = createUser("customer5@gmail.com", "Hoàng Bích Ngọc", "0903000005", encodedPassword, Set.of(roleUser));
        User customer6 = createUser("customer6@gmail.com", "Lý Thanh Tùng", "0903000006", encodedPassword, Set.of(roleUser));

        log.info("Đã tạo thành công 10 Users mẫu.");

        // 3. Brands
        Brand brandApple = createBrand("Apple", "https://logo.clearbit.com/apple.com", "Tập đoàn công nghệ Apple");
        Brand brandSamsung = createBrand("Samsung", "https://logo.clearbit.com/samsung.com", "Tập đoàn điện tử Samsung");
        Brand brandSony = createBrand("Sony", "https://logo.clearbit.com/sony.com", "Thương hiệu Sony Nhật Bản");
        Brand brandNike = createBrand("Nike", "https://logo.clearbit.com/nike.com", "Thương hiệu thể thao Nike");
        Brand brandAdidas = createBrand("Adidas", "https://logo.clearbit.com/adidas.com", "Thương hiệu thể thao Adidas");

        // 4. Categories
        Category catElectronics = createCategory("Điện thoại & Phụ kiện", "Các dòng smartphone và phụ kiện đi kèm");
        Category catComputers = createCategory("Máy tính & Laptop", "Laptop, PC máy tính bàn và phụ kiện");
        Category catFashion = createCategory("Thời trang Nam/Nữ", "Quần áo, giày dép thời trang cao cấp");
        Category catHome = createCategory("Thiết bị Gia dụng", "Đồ dùng gia đình, thiết bị thông minh");

        // 5. Shops (3 Shops)
        Shop shop1 = createShop(vendor1.getId(), "TechZone Official Store", "Chuyên đồ công nghệ cao cấp chính hãng", ShopStatus.ACTIVE);
        Shop shop2 = createShop(vendor2.getId(), "Fashion Hub Vietnam", "Thời trang xu hướng mới nhất năm 2026", ShopStatus.ACTIVE);
        Shop shop3 = createShop(vendor3.getId(), "Gia Dụng Xanh Store", "Thiết bị nhà bếp và gia đình tiện ích", ShopStatus.ACTIVE);

        // 6. Products, ProductImages, ProductVariants
        Product p1 = createProduct(
                shop1.getId(), "iPhone 15 Pro Max 256GB", "Điện thoại Apple iPhone 15 Pro Max khung Titan, Chip A17 Pro",
                new BigDecimal("29990000"), 50, catElectronics, brandApple,
                List.of(
                        ProductImage.builder().imageUrl("https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600").isThumbnail(true).sortOrder(0).build(),
                        ProductImage.builder().imageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600").isThumbnail(false).sortOrder(1).build()
                ),
                List.of(
                        ProductVariant.builder().sku("IP15PM-TITAN-256").price(new BigDecimal("29990000")).stockQuantity(30).attributes(Map.of("Color", "Titanium", "Storage", "256GB")).status(ProductStatus.ACTIVE).build(),
                        ProductVariant.builder().sku("IP15PM-BLACK-256").price(new BigDecimal("29990000")).stockQuantity(20).attributes(Map.of("Color", "Black", "Storage", "256GB")).status(ProductStatus.ACTIVE).build()
                )
        );

        Product p2 = createProduct(
                shop1.getId(), "MacBook Pro M3 14 inch", "Laptop Apple MacBook Pro M3 8-Core CPU 10-Core GPU 8GB 512GB",
                new BigDecimal("39990000"), 30, catComputers, brandApple,
                List.of(
                        ProductImage.builder().imageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600").isThumbnail(true).sortOrder(0).build()
                ),
                List.of(
                        ProductVariant.builder().sku("MBP-M3-GRAY-512").price(new BigDecimal("39990000")).stockQuantity(30).attributes(Map.of("Color", "Space Gray", "RAM", "8GB")).status(ProductStatus.ACTIVE).build()
                )
        );

        Product p3 = createProduct(
                shop2.getId(), "Áo Polo Nam Nike Dri-FIT", "Áo thun thể thao Polo Nike thoáng khí, chất liệu cao cấp",
                new BigDecimal("890000"), 100, catFashion, brandNike,
                List.of(
                        ProductImage.builder().imageUrl("https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?w=600").isThumbnail(true).sortOrder(0).build()
                ),
                List.of(
                        ProductVariant.builder().sku("NIKE-POLO-WHITE-L").price(new BigDecimal("890000")).stockQuantity(50).attributes(Map.of("Color", "White", "Size", "L")).status(ProductStatus.ACTIVE).build(),
                        ProductVariant.builder().sku("NIKE-POLO-BLACK-M").price(new BigDecimal("890000")).stockQuantity(50).attributes(Map.of("Color", "Black", "Size", "M")).status(ProductStatus.ACTIVE).build()
                )
        );

        Product p4 = createProduct(
                shop2.getId(), "Giày Sneaker Adidas Ultraboost 5.0", "Giày chạy bộ Adidas Ultraboost đế đệm Boost êm ái",
                new BigDecimal("3200000"), 40, catFashion, brandAdidas,
                List.of(
                        ProductImage.builder().imageUrl("https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=600").isThumbnail(true).sortOrder(0).build()
                ),
                List.of(
                        ProductVariant.builder().sku("ADI-ULTRA-42").price(new BigDecimal("3200000")).stockQuantity(20).attributes(Map.of("Color", "Core Black", "Size", "42")).status(ProductStatus.ACTIVE).build()
                )
        );

        Product p5 = createProduct(
                shop3.getId(), "Nồi chiên không dầu Sony AirFryer 5.5L", "Nồi chiên không dầu công nghệ sấy giòn đối lưu 360 độ",
                new BigDecimal("1850000"), 60, catHome, brandSony,
                List.of(
                        ProductImage.builder().imageUrl("https://images.unsplash.com/photo-1585515320310-259814833e62?w=600").isThumbnail(true).sortOrder(0).build()
                ),
                List.of(
                        ProductVariant.builder().sku("SONY-FRYER-55L").price(new BigDecimal("1850000")).stockQuantity(60).attributes(Map.of("Capacity", "5.5L")).status(ProductStatus.ACTIVE).build()
                )
        );

        log.info("Đã tạo thành công 5 Sản phẩm mẫu kèm Variants & Images.");

        // 7. Carts & CartItems for Customers
        Cart cart1 = createCart(customer1.getId());
        addCartItem(cart1, p1.getVariants().get(0).getId(), 1);
        addCartItem(cart1, p3.getVariants().get(0).getId(), 2);

        Cart cart2 = createCart(customer2.getId());
        addCartItem(cart2, p2.getVariants().get(0).getId(), 1);

        log.info("Đã khởi tạo Giỏ hàng mẫu cho Khách hàng.");

        // 8. Orders, OrderItems & Payments
        // Order Group 1: Delivered Order
        String group1 = UUID.randomUUID().toString();
        Order order1 = createOrder(customer1.getId(), shop1.getId(), group1, OrderStatus.DELIVERED, "123 Đường Nguyễn Huệ, Q1, TP.HCM", "VNPAY", new BigDecimal("29990000"));
        addOrderItem(order1, p1.getVariants().get(0).getId(), "iPhone 15 Pro Max 256GB", 1, new BigDecimal("29990000"));
        createPayment(group1, order1.getId(), new BigDecimal("29990000"), PaymentMethod.VNPAY, PaymentStatus.SUCCESS, "PAY_" + UUID.randomUUID().toString().substring(0, 8));

        // Order Group 2: Confirmed Order (Ready for vendor processing)
        String group2 = UUID.randomUUID().toString();
        Order order2 = createOrder(customer2.getId(), shop2.getId(), group2, OrderStatus.CONFIRMED, "456 Đường Cầu Giấy, Hà Nội", "COD", new BigDecimal("890000"));
        addOrderItem(order2, p3.getVariants().get(0).getId(), "Áo Polo Nam Nike Dri-FIT", 1, new BigDecimal("890000"));
        createPayment(group2, order2.getId(), new BigDecimal("890000"), PaymentMethod.COD, PaymentStatus.PENDING, "PAY_" + UUID.randomUUID().toString().substring(0, 8));

        // Order Group 3: Shipped Order
        String group3 = UUID.randomUUID().toString();
        Order order3 = createOrder(customer3.getId(), shop3.getId(), group3, OrderStatus.SHIPPED, "789 Đường Trần Hưng Đạo, Đà Nẵng", "VNPAY", new BigDecimal("1850000"));
        addOrderItem(order3, p5.getVariants().get(0).getId(), "Nồi chiên không dầu Sony AirFryer 5.5L", 1, new BigDecimal("1850000"));
        createPayment(group3, order3.getId(), new BigDecimal("1850000"), PaymentMethod.VNPAY, PaymentStatus.SUCCESS, "PAY_" + UUID.randomUUID().toString().substring(0, 8));

        log.info("=== HOÀN TẤT NẠP DỮ LIỆU MẪU TOÀN HỆ THỐNG PHỦ 13 BANG ===");
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
    }

    private User createUser(String email, String fullName, String phone, String encodedPassword, Set<Role> roles) {
        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .phone(phone)
                .passwordHash(encodedPassword)
                .status("ACTIVE")
                .roles(roles)
                .build();
        return userRepository.save(user);
    }

    private Brand createBrand(String name, String logoUrl, String description) {
        Brand brand = Brand.builder().name(name).logoUrl(logoUrl).description(description).build();
        return brandRepository.save(brand);
    }

    private Category createCategory(String name, String description) {
        Category category = Category.builder().name(name).description(description).build();
        return categoryRepository.save(category);
    }

    private Shop createShop(Long userId, String name, String description, ShopStatus status) {
        Shop shop = Shop.builder().userId(userId).name(name).description(description).status(status).build();
        return shopRepository.save(shop);
    }

    private Product createProduct(Long shopId, String name, String description, BigDecimal price, Integer stock,
                                   Category category, Brand brand, List<ProductImage> images, List<ProductVariant> variants) {
        Product product = Product.builder()
                .shopId(shopId)
                .name(name)
                .description(description)
                .price(price)
                .stockQuantity(stock)
                .category(category)
                .brand(brand)
                .status(ProductStatus.ACTIVE)
                .build();

        for (ProductImage img : images) {
            img.setProduct(product);
        }
        product.setImages(images);

        for (ProductVariant v : variants) {
            v.setProduct(product);
        }
        product.setVariants(variants);

        return productRepository.save(product);
    }

    private Cart createCart(Long userId) {
        Cart cart = Cart.builder().userId(userId).build();
        return cartRepository.save(cart);
    }

    private void addCartItem(Cart cart, Long variantId, Integer quantity) {
        CartItem item = CartItem.builder().cart(cart).productVariantId(variantId).quantity(quantity).build();
        cart.getItems().add(item);
        cartRepository.save(cart);
    }

    private Order createOrder(Long userId, Long shopId, String paymentGroupId, OrderStatus status, String address, String paymentMethod, BigDecimal total) {
        Order order = Order.builder()
                .userId(userId)
                .shopId(shopId)
                .paymentGroupId(paymentGroupId)
                .status(status)
                .shippingAddress(address)
                .paymentMethod(paymentMethod)
                .totalAmount(total)
                .build();
        return orderRepository.save(order);
    }

    private void addOrderItem(Order order, Long variantId, String name, Integer qty, BigDecimal price) {
        OrderItem item = OrderItem.builder()
                .order(order)
                .productVariantId(variantId)
                .productName(name)
                .quantity(qty)
                .unitPrice(price)
                .subTotal(price.multiply(BigDecimal.valueOf(qty)))
                .build();
        order.addOrderItem(item);
        orderRepository.save(order);
    }

    private void createPayment(String paymentGroupId, Long orderId, BigDecimal amount, PaymentMethod method, PaymentStatus status, String txnId) {
        Payment payment = Payment.builder()
                .paymentGroupId(paymentGroupId)
                .orderId(orderId)
                .amount(amount)
                .method(method)
                .status(status)
                .transactionId(txnId)
                .gatewayTransactionId("GW_" + txnId)
                .rawData("{\"code\":\"00\",\"msg\":\"Success\"}")
                .build();
        paymentRepository.save(payment);
    }
}
