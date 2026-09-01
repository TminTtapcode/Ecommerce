package com.tmt.ecommerce.voucher.api;

import com.tmt.ecommerce.voucher.entity.Voucher;

public interface VoucherInternalService {

    /**
     * Lấy và validate voucher theo code (kiểm tra hạn sử dụng, usage limit, chưa kiểm tra minOrderValue)
     */
    Voucher validateAndGetVoucher(String code);

    /**
     * Tăng số lượt sử dụng của voucher bằng Atomic Conditional Update.
     * @return true nếu thành công, false nếu voucher đã hết lượt.
     */
    boolean incrementUsage(Long voucherId);
}
