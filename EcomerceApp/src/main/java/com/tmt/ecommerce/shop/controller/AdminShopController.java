package com.tmt.ecommerce.shop.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/shops")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<com.tmt.ecommerce.shop.dto.ShopResponse>>> getShops(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<com.tmt.ecommerce.shop.dto.ShopResponse> shopPage = shopService.getAdminShops(status, page, size);

        return ResponseEntity.ok(ApiResponse.<Page<com.tmt.ecommerce.shop.dto.ShopResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách gian hàng thành công")
                .data(shopPage)
                .build());
    }

    @PutMapping("/{shopId}/approve")

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveShop(@PathVariable Long shopId) {

        shopService.approveShop(shopId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã phê duyệt gian hàng thành công.")
                .build());
    }

    @PutMapping("/{shopId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> banShop(
            @PathVariable Long shopId,
            @com.tmt.ecommerce.common.annotation.CurrentUserId Long actorId,
            @jakarta.validation.Valid @RequestBody(required = false) com.tmt.ecommerce.shop.dto.ShopBanRequest body) {

        String reason = body != null ? body.reason() : null;
        shopService.banShop(actorId, shopId, reason);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã khóa gian hàng thành công.")
                .build());
    }

    @PutMapping("/{shopId}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unbanShop(@com.tmt.ecommerce.common.annotation.CurrentUserId Long actorId, @PathVariable Long shopId) {

        shopService.unbanShop(actorId, shopId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã mở khóa gian hàng thành công.")
                .build());
    }
}
