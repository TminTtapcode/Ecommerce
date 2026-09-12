package com.tmt.ecommerce.voucher.service;

import org.springframework.data.domain.Page;
import com.tmt.ecommerce.voucher.dto.request.VoucherCreateRequest;
import com.tmt.ecommerce.voucher.dto.request.VoucherUpdateRequest;
import com.tmt.ecommerce.voucher.dto.response.VoucherResponse;

public interface VoucherService {
    VoucherResponse createVoucher(Long userId, VoucherCreateRequest request);
    Page<VoucherResponse> getShopVouchers(Long userId, int page, int size);
    Page<VoucherResponse> getSystemVouchers(int page, int size);
    VoucherResponse updateVoucher(Long userId, Long voucherId, VoucherUpdateRequest request);
    void deleteVoucher(Long userId, Long voucherId);
}
