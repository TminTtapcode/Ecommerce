package com.tmt.ecommerce.statistics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AdminDashboardStatsResponse(

    long totalUsers,
    long totalShops,
    long totalProducts,

    long totalOrders,
    Map<String, Long> ordersByStatus,

    BigDecimal deliveredRevenue,

    BigDecimal averageOrderValue,

    List<DailyChartPoint> dailyChart,

    List<RecentOrder> recentOrders
) {
    public record DailyChartPoint(
        String date,
        long orderCount,
        BigDecimal revenue
    ) {}

    public record RecentOrder(
        Long id,
        Long userId,
        Long shopId,
        String status,
        BigDecimal totalAmount,
        String paymentMethod,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        java.time.LocalDateTime createdAt
    ) {}
}
