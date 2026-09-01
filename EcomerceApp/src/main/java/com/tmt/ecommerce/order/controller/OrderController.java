package com.tmt.ecommerce.order.controller;

import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.tmt.ecommerce.order.dto.response.CheckoutResponse;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @CurrentUserId Long userId,
            @Valid @RequestBody CheckoutRequest request) {

        // Gọi xuống Service để xử lý logic tách đơn
        CheckoutResponse checkoutResponse = orderService.checkout(userId, request);

        ApiResponse<CheckoutResponse> response = ApiResponse.<CheckoutResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo đơn hàng thành công")
                .data(checkoutResponse)
                .build();

        // Trả về HTTP Status 201 (Created) cùng với kết quả checkout
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<com.tmt.ecommerce.cart.dto.response.CartPreviewResponse>> previewCheckout(
            @CurrentUserId Long userId,
            @Valid @RequestBody com.tmt.ecommerce.cart.dto.request.CartPreviewRequest request) {

        com.tmt.ecommerce.cart.dto.response.CartPreviewResponse response = orderService.previewCheckout(userId, request);

        return ResponseEntity.ok(ApiResponse.<com.tmt.ecommerce.cart.dto.response.CartPreviewResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Tính toán giỏ hàng thành công")
                .data(response)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<OrderResponse>>> getMyOrders(
            @CurrentUserId Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        org.springframework.data.domain.Page<OrderResponse> orderPage = orderService.getMyOrders(userId, status, page, size);

        ApiResponse<org.springframework.data.domain.Page<OrderResponse>> response = ApiResponse.<org.springframework.data.domain.Page<OrderResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách đơn hàng thành công")
                .data(orderPage)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @CurrentUserId Long userId,
            @PathVariable Long id) {

        OrderResponse orderDetail = orderService.getOrderDetail(userId, id);

        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy chi tiết đơn hàng thành công")
                .data(orderDetail)
                .build();

        return ResponseEntity.ok(response);
    }
}