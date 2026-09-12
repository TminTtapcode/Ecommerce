package com.tmt.ecommerce.order.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Shop-scoped order aggregates for the vendor analytics read model.
 * Monetary values describe order snapshots, not cash collection or settlement.
 */
public record ShopOrderAnalyticsDto(
        long totalOrders,
        Map<String, Long> ordersByStatus,
        long deliveredOrderCount,
        BigDecimal fulfilledGrossOrderValue,
        BigDecimal voucherDiscountAmount,
        BigDecimal fulfilledOrderValue,
        BigDecimal averageFulfilledOrderValue,
        List<DailyChartPointDto> dailyChart
) {
    public record DailyChartPointDto(
            String date,
            long createdOrderCount,
            BigDecimal fulfilledOrderValue
    ) {}
}
