package com.tmt.ecommerce.shop.service;

import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
                .map(Shop::getId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng chưa sở hữu bất kỳ Shop nào."));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findShopIdByUserId(Long userId) {
        return shopRepository.findByUserId(userId).map(Shop::getId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getUserIdByShopId(Long shopId) {
        return shopRepository.findById(shopId).map(Shop::getUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getShopNameById(Long shopId) {
        return shopRepository.findById(shopId).map(Shop::getName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getBannedShopIds() {
        return shopRepository.findBannedIds();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void requireNotBannedForSale(Long shopId) {
        var shop = shopRepository.findByIdForSale(shopId).orElseThrow(() ->
                new AppException(ErrorCode.SHOP_NOT_FOUND));
        if (shop.getStatus() == ShopStatus.BANNED) {
            throw new AppException(ErrorCode.SHOP_NOT_ACTIVE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countShops() {
        return shopRepository.count();
    }
}

