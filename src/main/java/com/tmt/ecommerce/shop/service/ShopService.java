package com.tmt.ecommerce.shop.service;

import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.shop.api.event.ShopApprovedEvent;
import com.tmt.ecommerce.shop.dto.ShopCreateRequest;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final IdentityInternalService identityInternalService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createShop(Long userId, ShopCreateRequest request) {

        // Rule 1: User này đã có shop chưa?
        if (shopRepository.existsByUserId(userId)) {
            throw new AppException(ErrorCode.SHOP_ALREADY_EXISTS);
        }

        // Rule 2: Tên shop đã tồn tại trong hệ thống chưa?
        if (shopRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.SHOP_NAME_DUPLICATE);
        }

        // Tạo Shop mới
        Shop newShop = Shop.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .status(ShopStatus.PENDING) // Luôn ở trạng thái chờ Admin duyệt
                .build();

        shopRepository.save(newShop);

        // Ghi chú: Sau này khi học đến module phân quyền sâu hơn,
        // chúng ta sẽ gọi logic thêm ROLE_SHOP_OWNER cho User tại đây.
    }
    // Thêm các dependency này vào đầu class ShopService


    @Transactional
    public void approveShop(Long shopId) {
        Shop shop = shopRepository.findByIdForUpdate(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new AppException(ErrorCode.SHOP_INVALID_STATUS_TRANSITION);
        }

        // Cập nhật trạng thái
        shop.setStatus(ShopStatus.ACTIVE);
        shopRepository.save(shop);

        // Cấp quyền cho User thông qua Internal API
        String userEmail = identityInternalService.assignShopOwnerRole(shop.getUserId());

        // Publish semantic domain event - email will only be dispatched AFTER_COMMIT
        eventPublisher.publishEvent(new ShopApprovedEvent(shop.getId(), shop.getUserId(), userEmail, shop.getName()));
    }

    @Transactional
    public void banShop(Long actorId, Long shopId, String reason) {
        Shop shop = shopRepository.findByIdForUpdate(shopId)
                .orElseThrow(() -> new com.tmt.ecommerce.common.exception.AppException(
                        com.tmt.ecommerce.common.exception.ErrorCode.SHOP_NOT_FOUND, "Không tìm thấy gian hàng."));

        if (shop.getStatus() == ShopStatus.BANNED) {
            throw new com.tmt.ecommerce.common.exception.AppException(
                    com.tmt.ecommerce.common.exception.ErrorCode.SHOP_INVALID_STATUS_TRANSITION, "Gian hàng này hiện đã bị khóa.");
        }

        if (reason != null && reason.length() > 500) throw new AppException(ErrorCode.INVALID_INPUT);
        ShopStatus previous = shop.getStatus();
        shop.setPriorStatus(previous);
        shop.setStatus(ShopStatus.BANNED);
        eventPublisher.publishEvent(new com.tmt.ecommerce.shop.api.event.ShopStatusChangedEvent(
                actorId, shopId, previous, ShopStatus.BANNED, reason));
        shopRepository.save(shop);
    }

    @Transactional
    public void unbanShop(Long actorId, Long shopId) {
        Shop shop = shopRepository.findByIdForUpdate(shopId)
                .orElseThrow(() -> new com.tmt.ecommerce.common.exception.AppException(
                        com.tmt.ecommerce.common.exception.ErrorCode.SHOP_NOT_FOUND, "Không tìm thấy gian hàng."));

        if (shop.getStatus() != ShopStatus.BANNED) {
            throw new com.tmt.ecommerce.common.exception.AppException(
                    com.tmt.ecommerce.common.exception.ErrorCode.SHOP_INVALID_STATUS_TRANSITION, "Gian hàng này không ở trạng thái bị khóa.");
        }

        ShopStatus restored = shop.getPriorStatus();
        if (restored != ShopStatus.ACTIVE && restored != ShopStatus.PENDING) {
            throw new AppException(ErrorCode.SHOP_PRIOR_STATUS_MISSING);
        }
        shop.setStatus(restored);
        shop.setPriorStatus(null);
        eventPublisher.publishEvent(new com.tmt.ecommerce.shop.api.event.ShopStatusChangedEvent(
                actorId, shopId, ShopStatus.BANNED, restored, null));
        shopRepository.save(shop);
    }

    @Transactional(readOnly = true)
    public com.tmt.ecommerce.shop.dto.ShopResponse getMyShop(Long userId) {
        Shop shop = shopRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        return com.tmt.ecommerce.shop.dto.ShopResponse.builder()
                .id(shop.getId())
                .userId(shop.getUserId())
                .name(shop.getName())
                .description(shop.getDescription())
                .status(shop.getStatus())
                .priorStatus(shop.getPriorStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<com.tmt.ecommerce.shop.dto.ShopResponse> getAdminShops(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Shop> shopPage;

        if (status != null && !status.trim().isEmpty()) {
            try {
                ShopStatus shopStatus = ShopStatus.valueOf(status.toUpperCase());
                shopPage = shopRepository.findByStatus(shopStatus, pageable);
            } catch (IllegalArgumentException e) {
                shopPage = shopRepository.findAll(pageable);
            }
        } else {
            shopPage = shopRepository.findAll(pageable);
        }

        return shopPage.map(shop -> com.tmt.ecommerce.shop.dto.ShopResponse.builder()
                .id(shop.getId())
                .userId(shop.getUserId())
                .name(shop.getName())
                .description(shop.getDescription())
                .status(shop.getStatus())
                .priorStatus(shop.getPriorStatus())
                .build());
    }

    @Transactional(readOnly = true)
    public com.tmt.ecommerce.shop.dto.ShopResponse getShopPublicInfo(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new AppException(ErrorCode.SHOP_NOT_ACTIVE);
        }

        return com.tmt.ecommerce.shop.dto.ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .description(shop.getDescription())
                .build();
    }
}
