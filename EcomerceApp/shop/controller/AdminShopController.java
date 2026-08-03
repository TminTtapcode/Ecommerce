package com.tmt.ecommerce.shop.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/shops")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;

    @PutMapping("/{shopId}/approve")
    // Chỉ những User mang token có ROLE_ADMIN mới được gọi API này
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveShop(@PathVariable Long shopId) {

        shopService.approveShop(shopId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã phê duyệt gian hàng thành công.")
                .build());
    }
}