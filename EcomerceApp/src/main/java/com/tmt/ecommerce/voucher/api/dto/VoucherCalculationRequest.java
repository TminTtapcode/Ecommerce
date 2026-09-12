package com.tmt.ecommerce.voucher.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record VoucherCalculationRequest(
    String voucherCode,

    Map<Long, BigDecimal> shopTotals
) {}
