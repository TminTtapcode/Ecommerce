package com.tmt.ecommerce.payout.controller;

import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.payout.dto.request.ApprovePayoutRequest;
import com.tmt.ecommerce.payout.dto.request.RejectPayoutRequest;
import com.tmt.ecommerce.payout.dto.response.PayoutResponse;
import com.tmt.ecommerce.payout.enums.PayoutStatus;
import com.tmt.ecommerce.payout.service.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payouts")
@RequiredArgsConstructor
public class AdminPayoutController {

    private final PayoutService payoutService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getPayouts(
            @RequestParam(required = false) PayoutStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PayoutResponse> response = payoutService.getAdminPayouts(status, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PayoutResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách yêu cầu rút tiền thành công")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutResponse>> approvePayout(
            @PathVariable Long id,
            @CurrentUserId Long adminUserId,
            @RequestBody(required = false) ApprovePayoutRequest request) {
        PayoutResponse response = payoutService.approvePayout(id, adminUserId, request);
        return ResponseEntity.ok(ApiResponse.<PayoutResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Phê duyệt yêu cầu rút tiền thành công")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutResponse>> rejectPayout(
            @PathVariable Long id,
            @CurrentUserId Long adminUserId,
            @Valid @RequestBody RejectPayoutRequest request) {
        PayoutResponse response = payoutService.rejectPayout(id, adminUserId, request);
        return ResponseEntity.ok(ApiResponse.<PayoutResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Từ chối yêu cầu rút tiền thành công")
                .data(response)
                .build());
    }
}
