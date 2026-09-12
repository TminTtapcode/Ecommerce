package com.tmt.ecommerce.shop.api;

public interface ShopInternalService {
    java.util.List<Long> getBannedShopIds();

    void requireNotBannedForSale(Long shopId);

    boolean isShopOwner(Long shopId, Long userId);

    Long getShopIdByUserId(Long userId);

    java.util.Optional<Long> findShopIdByUserId(Long userId);

    java.util.Optional<Long> getUserIdByShopId(Long shopId);

    java.util.Optional<String> getShopNameById(Long shopId);

    long countShops();
}
