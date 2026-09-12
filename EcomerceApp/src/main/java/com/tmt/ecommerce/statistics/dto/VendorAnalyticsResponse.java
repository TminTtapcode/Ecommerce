package com.tmt.ecommerce.statistics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record VendorAnalyticsResponse(
        String dateBasis,
        LocalDate startDate,
        LocalDate endDate,
        long totalOrders,
        Map<String, Long> ordersByStatus,
        long deliveredOrderCount,
        BigDecimal fulfilledGrossOrderValue,
        BigDecimal voucherDiscountAmount,
        BigDecimal fulfilledOrderValue,
        BigDecimal averageFulfilledOrderValue,
        List<DailyChartPoint> dailyChart
) {
    public record DailyChartPoint(String date, long createdOrderCount, BigDecimal fulfilledOrderValue) {}
}
