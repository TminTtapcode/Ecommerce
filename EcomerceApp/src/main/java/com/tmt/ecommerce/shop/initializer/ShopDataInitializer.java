package com.tmt.ecommerce.shop.initializer;

import com.tmt.ecommerce.common.constant.SeedDataIds;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ShopDataInitializer implements CommandLineRunner {

    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (shopRepository.count() > 0) {
            log.info("Shops already exist. Skipping ShopDataInitializer.");
            return;
        }

        log.info("=== STARTING SHOP SEED DATA ===");

        createShop(SeedDataIds.VENDOR_1_ID, "TechZone Official Store", "Chuyên đồ công nghệ cao cấp chính hãng", ShopStatus.ACTIVE);
        createShop(SeedDataIds.VENDOR_2_ID, "Fashion Hub Vietnam", "Thời trang xu hướng mới nhất năm 2026", ShopStatus.ACTIVE);
        createShop(SeedDataIds.VENDOR_3_ID, "Gia Dụng Xanh Store", "Thiết bị nhà bếp và gia đình tiện ích", ShopStatus.ACTIVE);

        log.info("=== FINISHED SHOP SEED DATA ===");
    }

    private Shop createShop(Long userId, String name, String description, ShopStatus status) {
        Shop shop = Shop.builder()
                .userId(userId)
                .name(name)
                .description(description)
                .status(status)
                .build();
        return shopRepository.save(shop);
    }
}
