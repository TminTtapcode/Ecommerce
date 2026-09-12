package com.tmt.ecommerce.voucher.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record VoucherDiscountResult(
    Long voucherId,
    String code,
    Map<Long, BigDecimal> discountPerShop
) {}
