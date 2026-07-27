package com.tmt.ecommerce.shop.service;

import com.tmt.ecommerce.common.service.EmailService;
import com.tmt.ecommerce.identity.entity.Role;
import com.tmt.ecommerce.identity.entity.User;
import com.tmt.ecommerce.identity.repository.RoleRepository;
import com.tmt.ecommerce.identity.repository.UserRepository;
import com.tmt.ecommerce.shop.dto.ShopCreateRequest;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
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
                .status("PENDING") // Luôn ở trạng thái chờ Admin duyệt
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

        if (!"PENDING".equals(shop.getStatus())) {
            throw new IllegalStateException("Gian hàng này không ở trạng thái chờ duyệt.");
        }

        // Cập nhật trạng thái
        shop.setStatus("ACTIVE");
        shopRepository.save(shop);

        // Cấp quyền cho User
        User user = userRepository.findById(shop.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ sở hữu."));

        Role shopRole = roleRepository.findByName("ROLE_SHOP_OWNER")
                .orElseThrow(() -> new IllegalStateException("Chưa cấu hình quyền ROLE_SHOP_OWNER."));

        user.getRoles().add(shopRole);
        userRepository.save(user);

        // Gửi email thông báo chạy ngầm
        emailService.sendShopApprovalNotification(user.getEmail(), shop.getName());
    }
}