package com.tmt.ecommerce.voucher.api;

import com.tmt.ecommerce.voucher.entity.Voucher;
import com.tmt.ecommerce.voucher.api.dto.VoucherCalculationRequest;
import com.tmt.ecommerce.voucher.api.dto.VoucherDiscountResult;

public interface VoucherInternalService {

    VoucherDiscountResult calculateDiscount(VoucherCalculationRequest request);

    boolean incrementUsage(Long voucherId);
}
