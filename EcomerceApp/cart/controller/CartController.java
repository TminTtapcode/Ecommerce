package com.tmt.ecommerce.cart.controller;

import com.tmt.ecommerce.cart.dto.request.CartItemRequest;
import com.tmt.ecommerce.cart.dto.request.CartItemUpdateRequest;
import com.tmt.ecommerce.cart.dto.response.CartResponse;
import com.tmt.ecommerce.cart.service.CartService;
import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // API: Xem giỏ hàng
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(@CurrentUserId Long userId) {
        CartResponse cartResponse = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin giỏ hàng thành công")
                .data(cartResponse)
                .build());
    }

    // API: Thêm vào giỏ hàng
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Void>> addToCart(
            @CurrentUserId Long userId,
            @Valid @RequestBody CartItemRequest request) {

        cartService.addToCart(userId, request);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã thêm sản phẩm vào giỏ hàng")
                .build());
    }
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> updateCartItemQuantity(
            @CurrentUserId Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateRequest request) {

        cartService.updateItemQuantity(userId, itemId, request.getQuantity());

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã cập nhật số lượng sản phẩm")
                .build());
    }

    // API: Xóa 1 sản phẩm khỏi giỏ
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @CurrentUserId Long userId,
            @PathVariable Long itemId) {

        cartService.removeCartItem(userId, itemId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã xóa sản phẩm khỏi giỏ hàng")
                .build());
    }

    // API: Xóa toàn bộ giỏ hàng
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@CurrentUserId Long userId) {
        cartService.clearCart(userId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã dọn sạch giỏ hàng")
                .build());
    }
}