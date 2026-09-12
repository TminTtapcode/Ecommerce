package com.tmt.ecommerce.payment.controller;

import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.payment.dto.request.AdminRefundRequest;
import com.tmt.ecommerce.payment.dto.response.RefundAttemptResponse;
import com.tmt.ecommerce.payment.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundController {
    private final RefundService refunds;

    @PostMapping("/orders/{orderId}/refunds")
    public ApiResponse<RefundAttemptResponse> create(@PathVariable Long orderId, @CurrentUserId Long actorId,
            @RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody AdminRefundRequest request) {
        return ApiResponse.<RefundAttemptResponse>builder().status(HttpStatus.OK.value())
                .data(refunds.create(orderId, actorId, idempotencyKey, request.reason())).build();
    }

    @GetMapping("/orders/{orderId}/refunds")
    public ApiResponse<List<RefundAttemptResponse>> list(@PathVariable Long orderId) {
        return ApiResponse.<List<RefundAttemptResponse>>builder().status(HttpStatus.OK.value())
                .data(refunds.listForOrder(orderId)).build();
    }

    @GetMapping("/refunds/{refundId}")
    public ApiResponse<RefundAttemptResponse> get(@PathVariable Long refundId) {
        return ApiResponse.<RefundAttemptResponse>builder().status(HttpStatus.OK.value()).data(refunds.get(refundId)).build();
    }

    @PostMapping("/refunds/{refundId}/reconcile")
    public ApiResponse<RefundAttemptResponse> reconcile(@PathVariable Long refundId) {
        return ApiResponse.<RefundAttemptResponse>builder().status(HttpStatus.OK.value()).data(refunds.reconcile(refundId)).build();
    }
}
