package com.tmt.ecommerce.shop.service;

import com.tmt.ecommerce.shop.api.ShopInternalService;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopInternalServiceImpl implements ShopInternalService {

    private final ShopRepository shopRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isShopOwner(Long shopId, Long userId) {
        return shopRepository.existsByIdAndUserId(shopId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getShopIdByUserId(Long userId) {
        return shopRepository.findByUserId(userId)
                .map(shop -> shop.getId())
                .orElseThrow(() -> new IllegalArgumentException("Người dùng chưa sở hữu bất kỳ Shop nào."));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Long> findShopIdByUserId(Long userId) {
        return shopRepository.findByUserId(userId).map(shop -> shop.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Long> getUserIdByShopId(Long shopId) {
        return shopRepository.findById(shopId).map(shop -> shop.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<String> getShopNameById(Long shopId) {
        return shopRepository.findById(shopId).map(shop -> shop.getName());
    }
    @Override
    @Transactional(readOnly = true)
    public java.util.List<Long> getBannedShopIds() { return shopRepository.findBannedIds(); }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void requireNotBannedForSale(Long shopId) {
        var shop = shopRepository.findByIdForSale(shopId).orElseThrow(() ->
                new com.tmt.ecommerce.common.exception.AppException(com.tmt.ecommerce.common.exception.ErrorCode.SHOP_NOT_FOUND));
        if (shop.getStatus() == com.tmt.ecommerce.shop.enums.ShopStatus.BANNED) {
            throw new com.tmt.ecommerce.common.exception.AppException(com.tmt.ecommerce.common.exception.ErrorCode.SHOP_NOT_ACTIVE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countShops() {
        return shopRepository.count();
    }
}
