package com.tmt.ecommerce.voucher.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record VoucherCalculationRequest(
    String voucherCode,
    /**
     * shopTotals:
     * shopId → subtotal trước voucher discount của shop đó
     */
    Map<Long, BigDecimal> shopTotals
) {}
