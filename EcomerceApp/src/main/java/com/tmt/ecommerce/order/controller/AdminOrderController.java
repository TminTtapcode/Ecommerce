package com.tmt.ecommerce.order.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final com.tmt.ecommerce.order.service.AdminOrderService adminOrderService;

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> updateStatus(@PathVariable Long orderId,
            @jakarta.validation.Valid @RequestBody com.tmt.ecommerce.order.dto.request.AdminOrderStatusRequest request) {
        return ApiResponse.<OrderResponse>builder().status(200).message("Cập nhật trạng thái thành công")
                .data(adminOrderService.update(orderId, request)).build();
    }

    @GetMapping("/{orderId}/allowed-transitions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<java.util.List<com.tmt.ecommerce.order.entity.OrderStatus>> allowedTransitions(@PathVariable Long orderId) {
        return ApiResponse.<java.util.List<com.tmt.ecommerce.order.entity.OrderStatus>>builder().status(200)
                .data(adminOrderService.allowedTransitions(orderId)).build();
    }

    @GetMapping("/{orderId}/status-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<com.tmt.ecommerce.order.dto.response.OrderAdminHistoryResponse>> history(
            @PathVariable Long orderId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<com.tmt.ecommerce.order.dto.response.OrderAdminHistoryResponse>>builder().status(200)
                .data(adminOrderService.history(orderId, page, size)).build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<OrderResponse> orderPage = orderService.getAdminOrders(status, page, size);

        return ResponseEntity.ok(ApiResponse.<Page<OrderResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách đơn hàng thành công")
                .data(orderPage)
                .build());
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(@PathVariable Long orderId) {

        OrderResponse orderResponse = orderService.getAdminOrderDetail(orderId);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy chi tiết đơn hàng thành công")
                .data(orderResponse)
                .build());
    }
}
