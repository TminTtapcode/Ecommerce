package com.tmt.ecommerce.voucher.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.voucher.dto.response.VoucherResponse;
import com.tmt.ecommerce.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getSystemVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<VoucherResponse> voucherPage = voucherService.getSystemVouchers(page, size);

        return ResponseEntity.ok(ApiResponse.<Page<VoucherResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách mã giảm giá hệ thống thành công")
                .data(voucherPage)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @com.tmt.ecommerce.common.annotation.CurrentUserId Long userId,
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody com.tmt.ecommerce.voucher.dto.request.VoucherUpdateRequest request) {

        VoucherResponse response = voucherService.updateVoucher(userId, id, request);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật mã giảm giá thành công")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(
            @com.tmt.ecommerce.common.annotation.CurrentUserId Long userId,
            @PathVariable Long id) {

        voucherService.deleteVoucher(userId, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Xóa mã giảm giá thành công")
                .build());
    }
}
