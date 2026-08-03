package com.tmt.ecommerce.order.controller;

import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<List<OrderResponse>> checkout(
            // Tạm thời lấy userId từ Header để test, thực tế cậu có thể lấy từ SecurityContext (Spring Security)
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CheckoutRequest request) {

        // Gọi xuống Service để xử lý logic tách đơn
        List<OrderResponse> createdOrders = orderService.checkout(userId, request);

        // Trả về HTTP Status 201 (Created) cùng với danh sách đơn hàng vừa tạo
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrders);
    }
}