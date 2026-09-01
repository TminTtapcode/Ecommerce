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
}
