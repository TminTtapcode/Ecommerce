package com.tmt.ecommerce.voucher.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.voucher.dto.request.VoucherCreateRequest;
import com.tmt.ecommerce.voucher.dto.response.VoucherResponse;
import com.tmt.ecommerce.voucher.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

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

    @GetMapping("/my-shop")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getShopVouchers(
            @com.tmt.ecommerce.common.annotation.CurrentUserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<VoucherResponse> voucherPage = voucherService.getShopVouchers(userId, page, size);
        return ResponseEntity.ok(ApiResponse.<Page<VoucherResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách mã giảm giá thành công")
                .data(voucherPage)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateShopVoucher(
            @com.tmt.ecommerce.common.annotation.CurrentUserId Long userId,
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody com.tmt.ecommerce.voucher.dto.request.VoucherUpdateRequest request) {

        VoucherResponse response = voucherService.updateVoucher(userId, id, request);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật mã giảm giá gian hàng thành công")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteShopVoucher(
            @com.tmt.ecommerce.common.annotation.CurrentUserId Long userId,
            @PathVariable Long id) {

        voucherService.deleteVoucher(userId, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Xóa mã giảm giá gian hàng thành công")
                .build());
    }
}
