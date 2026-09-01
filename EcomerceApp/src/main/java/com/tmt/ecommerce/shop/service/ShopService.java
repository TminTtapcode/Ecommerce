package com.tmt.ecommerce.shop.service;

import com.tmt.ecommerce.common.service.EmailService;
import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.shop.dto.ShopCreateRequest;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final IdentityInternalService identityInternalService;
    private final EmailService emailService; // <-- Inject EmailService vào đây

    @Transactional
    public void createShop(Long userId, ShopCreateRequest request) {

        // Rule 1: User này đã có shop chưa?
        if (shopRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Bạn đã sở hữu một gian hàng rồi.");
        }

        // Rule 2: Tên shop đã tồn tại trong hệ thống chưa?
        if (shopRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Tên shop đã được sử dụng. Vui lòng chọn tên khác.");
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
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gian hàng."));

        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new IllegalStateException("Gian hàng này không ở trạng thái chờ duyệt.");
        }

        // Cập nhật trạng thái
        shop.setStatus(ShopStatus.ACTIVE);
        shopRepository.save(shop);

        // Cấp quyền cho User thông qua Internal API
        String userEmail = identityInternalService.assignShopOwnerRole(shop.getUserId());

        // Gửi email thông báo chạy ngầm
        emailService.sendShopApprovalNotification(userEmail, shop.getName());
    }
}