package com.tmt.ecommerce.voucher.api;

import com.tmt.ecommerce.voucher.entity.Voucher;
import com.tmt.ecommerce.voucher.api.dto.VoucherCalculationRequest;
import com.tmt.ecommerce.voucher.api.dto.VoucherDiscountResult;

public interface VoucherInternalService {

    /**
     * Lấy và validate voucher, sau đó tính toán discount theo request (kiểm tra hạn, min value, usage, chia tỉ lệ)
     */
    VoucherDiscountResult calculateDiscount(VoucherCalculationRequest request);

    /**
     * Tăng số lượt sử dụng của voucher bằng Atomic Conditional Update.
     * @return true nếu thành công, false nếu voucher đã hết lượt.
     */
    boolean incrementUsage(Long voucherId);
}
