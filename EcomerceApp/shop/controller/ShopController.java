package com.tmt.ecommerce.shop.controller;

import com.tmt.ecommerce.common.annotation.CurrentUserId; // <-- Import Annotation mới
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
            @CurrentUserId Long userId, // <-- Xịn xò chưa! Mọi thứ tự động được Spring bơm vào đây
            @Valid @RequestBody ShopCreateRequest request) {

        shopService.createShop(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Đăng ký mở shop thành công, đang chờ duyệt.")
                        .build());
    }
}