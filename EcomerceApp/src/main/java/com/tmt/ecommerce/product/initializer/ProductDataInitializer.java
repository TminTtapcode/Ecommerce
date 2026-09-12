package com.tmt.ecommerce.product.initializer;

import com.tmt.ecommerce.common.constant.SeedDataIds;
import com.tmt.ecommerce.product.entity.*;
import com.tmt.ecommerce.product.enums.ProductStatus;
import com.tmt.ecommerce.product.repository.BrandRepository;
import com.tmt.ecommerce.product.repository.CategoryRepository;
import com.tmt.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class ProductDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() > 0 || brandRepository.count() > 0) {
            log.info("Products or Brands already exist. Skipping ProductDataInitializer.");
            return;
        }

        log.info("=== STARTING PRODUCT SEED DATA ===");

        Brand brandApple = createBrand("Apple", "https://logo.clearbit.com/apple.com", "Tập đoàn công nghệ Apple");
        Brand brandSamsung = createBrand("Samsung", "https://logo.clearbit.com/samsung.com", "Tập đoàn điện tử Samsung");
        Brand brandSony = createBrand("Sony", "https://logo.clearbit.com/sony.com", "Thương hiệu Sony Nhật Bản");
        Brand brandNike = createBrand("Nike", "https://logo.clearbit.com/nike.com", "Thương hiệu thể thao Nike");
        Brand brandAdidas = createBrand("Adidas", "https://logo.clearbit.com/adidas.com", "Thương hiệu thể thao Adidas");

        Category catElectronics = createCategory("Điện thoại & Phụ kiện", "Các dòng smartphone và phụ kiện đi kèm");
        Category catComputers = createCategory("Máy tính & Laptop", "Laptop, PC máy tính bàn và phụ kiện");
        Category catFashion = createCategory("Thời trang Nam/Nữ", "Quần áo, giày dép thời trang cao cấp");
        Category catHome = createCategory("Thiết bị Gia dụng", "Đồ dùng gia đình, thiết bị thông minh");

        createProduct(
                SeedDataIds.SHOP_1_ID, "iPhone 15 Pro Max 256GB", "Điện thoại Apple iPhone 15 Pro Max khung Titan, Chip A17 Pro",
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

        createProduct(
                SeedDataIds.SHOP_1_ID, "MacBook Pro M3 14 inch", "Laptop Apple MacBook Pro M3 8-Core CPU 10-Core GPU 8GB 512GB",
                new BigDecimal("39990000"), 30, catComputers, brandApple,
                List.of(
                        ProductImage.builder().imageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600").isThumbnail(true).sortOrder(0).build()
                ),
                List.of(
                        ProductVariant.builder().sku("MBP-M3-GRAY-512").price(new BigDecimal("39990000")).stockQuantity(30).attributes(Map.of("Color", "Space Gray", "RAM", "8GB")).status(ProductStatus.ACTIVE).build()
                )
        );

        createProduct(
                SeedDataIds.SHOP_2_ID, "Áo Polo Nam Nike Dri-FIT", "Áo thun thể thao Polo Nike thoáng khí, chất liệu cao cấp",
                new BigDecimal("890000"), 100, catFashion, brandNike,
                List.of(
                        ProductImage.builder().imageUrl("https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?w=600").isThumbnail(true).sortOrder(0).build()
                ),
                List.of(
                        ProductVariant.builder().sku("NIKE-POLO-WHITE-L").price(new BigDecimal("890000")).stockQuantity(50).attributes(Map.of("Color", "White", "Size", "L")).status(ProductStatus.ACTIVE).build(),
                        ProductVariant.builder().sku("NIKE-POLO-BLACK-M").price(new BigDecimal("890000")).stockQuantity(50).attributes(Map.of("Color", "Black", "Size", "M")).status(ProductStatus.ACTIVE).build()
                )
        );

        createProduct(
                SeedDataIds.SHOP_2_ID, "Giày Sneaker Adidas Ultraboost 5.0", "Giày chạy bộ Adidas Ultraboost đế đệm Boost êm ái",
                new BigDecimal("3200000"), 40, catFashion, brandAdidas,
                List.of(
                        ProductImage.builder().imageUrl("https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=600").isThumbnail(true).sortOrder(0).build()
                ),
                List.of(
                        ProductVariant.builder().sku("ADI-ULTRA-42").price(new BigDecimal("3200000")).stockQuantity(20).attributes(Map.of("Color", "Core Black", "Size", "42")).status(ProductStatus.ACTIVE).build()
                )
        );

        createProduct(
                SeedDataIds.SHOP_3_ID, "Nồi chiên không dầu Sony AirFryer 5.5L", "Nồi chiên không dầu công nghệ sấy giòn đối lưu 360 độ",
                new BigDecimal("1850000"), 60, catHome, brandSony,
                List.of(
                        ProductImage.builder().imageUrl("https://images.unsplash.com/photo-1585515320310-259814833e62?w=600").isThumbnail(true).sortOrder(0).build()
                ),
                List.of(
                        ProductVariant.builder().sku("SONY-FRYER-55L").price(new BigDecimal("1850000")).stockQuantity(60).attributes(Map.of("Capacity", "5.5L")).status(ProductStatus.ACTIVE).build()
                )
        );

        log.info("=== FINISHED PRODUCT SEED DATA ===");
    }

    private Brand createBrand(String name, String logoUrl, String description) {
        Brand brand = Brand.builder().name(name).logoUrl(logoUrl).description(description).build();
        return brandRepository.save(brand);
    }

    private Category createCategory(String name, String description) {
        Category category = Category.builder().name(name).description(description).build();
        return categoryRepository.save(category);
    }

    private void createProduct(Long shopId, String name, String description, BigDecimal price, Integer stock,
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

        productRepository.save(product);
    }
}
