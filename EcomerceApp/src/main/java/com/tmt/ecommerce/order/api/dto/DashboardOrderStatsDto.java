package com.tmt.ecommerce.order.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardOrderStatsDto(
    long totalOrders,
    Map<String, Long> ordersByStatus,
    BigDecimal deliveredRevenue,
    BigDecimal averageOrderValue,
    List<DailyChartPointDto> dailyChart,
    List<RecentOrderDto> recentOrders
) {
    public record DailyChartPointDto(String date, long orderCount, BigDecimal revenue) {}

    public record RecentOrderDto(
        Long id,
        Long userId,
        Long shopId,
        String status,
        BigDecimal totalAmount,
        String paymentMethod,
        java.time.LocalDateTime createdAt
    ) {}
}
