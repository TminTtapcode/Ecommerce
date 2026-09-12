package com.tmt.ecommerce.shop.controller;

import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.shop.dto.ShopCreateRequest;
import com.tmt.ecommerce.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> registerShop(
            @CurrentUserId Long userId,
            @Valid @RequestBody ShopCreateRequest request) {

        shopService.createShop(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Đăng ký mở shop thành công, đang chờ duyệt.")
                        .build());
    }

    @GetMapping("/my-shop")
    public ResponseEntity<ApiResponse<com.tmt.ecommerce.shop.dto.ShopResponse>> getMyShop(
            @CurrentUserId Long userId) {

        com.tmt.ecommerce.shop.dto.ShopResponse shopResponse = shopService.getMyShop(userId);

        return ResponseEntity.ok(ApiResponse.<com.tmt.ecommerce.shop.dto.ShopResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin gian hàng thành công")
                .data(shopResponse)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<com.tmt.ecommerce.shop.dto.ShopResponse>> getShopPublicInfo(@PathVariable Long id) {
        com.tmt.ecommerce.shop.dto.ShopResponse shopResponse = shopService.getShopPublicInfo(id);

        return ResponseEntity.ok(ApiResponse.<com.tmt.ecommerce.shop.dto.ShopResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin Shop thành công")
                .data(shopResponse)
                .build());
    }
}
