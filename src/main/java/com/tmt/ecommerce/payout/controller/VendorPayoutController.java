package com.tmt.ecommerce.payout.controller;

import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.payout.dto.request.CreatePayoutRequest;
import com.tmt.ecommerce.payout.dto.response.PayoutBalanceResponse;
import com.tmt.ecommerce.payout.dto.response.PayoutResponse;
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
@RequestMapping("/api/v1/vendor/payouts")
@RequiredArgsConstructor
public class VendorPayoutController {

    private final PayoutService payoutService;

    @GetMapping("/balance")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<PayoutBalanceResponse>> getBalance(@CurrentUserId Long userId) {
        PayoutBalanceResponse response = payoutService.getShopBalance(userId);
        return ResponseEntity.ok(ApiResponse.<PayoutBalanceResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin số dư gian hàng thành công")
                .data(response)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<PayoutResponse>> createPayout(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreatePayoutRequest request) {
        PayoutResponse response = payoutService.createPayoutRequest(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PayoutResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo yêu cầu rút tiền thành công")
                .data(response)
                .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getMyPayouts(
            @CurrentUserId Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PayoutResponse> response = payoutService.getShopPayouts(userId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PayoutResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy lịch sử rút tiền thành công")
                .data(response)
                .build());
    }
}
