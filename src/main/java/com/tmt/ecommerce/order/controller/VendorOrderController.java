package com.tmt.ecommerce.order.controller;

import com.tmt.ecommerce.order.dto.request.VendorOrderStatusUpdateRequest;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendor/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
public class VendorOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getVendorOrders(
            @CurrentUserId Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<OrderResponse> orderPage = orderService.getVendorOrders(userId, status, page, size);

        ApiResponse<Page<OrderResponse>> response = ApiResponse.<Page<OrderResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách đơn hàng thành công")
                .data(orderPage)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getVendorOrderDetail(
            @CurrentUserId Long userId,
            @PathVariable Long id) {

        OrderResponse orderDetail = orderService.getVendorOrderDetail(userId, id);

        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy chi tiết đơn hàng thành công")
                .data(orderDetail)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @CurrentUserId Long userId,
            @PathVariable Long id,
            @Valid @RequestBody VendorOrderStatusUpdateRequest request) {

        OrderResponse updatedOrder = orderService.updateVendorOrderStatus(userId, id, request);

        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật trạng thái đơn hàng thành công")
                .data(updatedOrder)
                .build();

        return ResponseEntity.ok(response);
    }
}
