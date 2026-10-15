package com.tmt.ecommerce.shop.api;

import java.util.List;
import java.util.Optional;

public interface ShopInternalService {
    List<Long> getBannedShopIds();

    void requireNotBannedForSale(Long shopId);

    boolean isShopOwner(Long shopId, Long userId);

    Long getShopIdByUserId(Long userId);

    Optional<Long> findShopIdByUserId(Long userId);

    Optional<Long> getUserIdByShopId(Long shopId);

    Optional<String> getShopNameById(Long shopId);

    long countShops();
}
