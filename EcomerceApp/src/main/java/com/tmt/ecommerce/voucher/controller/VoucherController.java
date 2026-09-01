package com.tmt.ecommerce.voucher.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.voucher.dto.request.VoucherCreateRequest;
import com.tmt.ecommerce.voucher.dto.response.VoucherResponse;
import com.tmt.ecommerce.voucher.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    // API tạo Voucher dành cho Admin hoặc Vendor
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @com.tmt.ecommerce.common.annotation.CurrentUserId Long userId,
            @Valid @RequestBody VoucherCreateRequest request) {
        VoucherResponse response = voucherService.createVoucher(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<VoucherResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Tạo mã giảm giá thành công")
                        .data(response)
                        .build()
        );
    }
}
