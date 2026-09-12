package com.tmt.ecommerce.statistics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Response DTO cho Admin Dashboard Statistics API.
 * GET /api/v1/admin/statistics/dashboard
 */
public record AdminDashboardStatsResponse(
    // ── Baseline counts ──────────────────────────────────────────────────
    long totalUsers,
    long totalShops,
    long totalProducts,

    // ── Order aggregates (filtered by date range if provided) ─────────────
    long totalOrders,
    Map<String, Long> ordersByStatus,

    /** Sum of totalAmount for DELIVERED orders only. */
    BigDecimal deliveredRevenue,

    /** Average order value = deliveredRevenue / count(DELIVERED). 0 if none. */
    BigDecimal averageOrderValue,

    // ── Daily Chart (order count per day, in range) ───────────────────────
    List<DailyChartPoint> dailyChart,

    // ── Recent Orders (always top-10 latest, regardless of date filter) ───
    List<RecentOrder> recentOrders
) {
    public record DailyChartPoint(
        String date,          // "YYYY-MM-DD"
        long orderCount,
        BigDecimal revenue    // DELIVERED revenue for that day (future use)
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
