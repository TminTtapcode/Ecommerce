package com.tmt.ecommerce.statistics.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.order.api.OrderInternalService;
import com.tmt.ecommerce.order.api.dto.DashboardOrderStatsDto;
import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import com.tmt.ecommerce.statistics.dto.AdminDashboardStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin Statistics Controller — P1-05 / Feature 2.
 * GET /api/v1/admin/statistics/dashboard
 *
 * Tập hợp dữ liệu từ các module thông qua internal service contracts.
 * Không truy cập repository của module khác trực tiếp.
 */
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final OrderInternalService orderInternalService;
    private final IdentityInternalService identityInternalService;
    private final ShopInternalService shopInternalService;
    private final ProductInternalService productInternalService;

    /**
     * Dashboard summary statistics.
     *
     * @param startDate optional start date (inclusive), format: yyyy-MM-dd
     * @param endDate   optional end date (inclusive), format: yyyy-MM-dd
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminDashboardStatsResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Convert LocalDate → LocalDateTime boundaries (inclusive day range)
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end   = endDate   != null ? endDate.atTime(LocalTime.MAX) : null;

        // ── Baseline counts (all-time, not date-filtered) ──────────────────
        long totalUsers    = identityInternalService.countUsers();
        long totalShops    = shopInternalService.countShops();
        long totalProducts = productInternalService.countProducts();

        // ── Order aggregates (date-filtered if params supplied) ─────────────
        DashboardOrderStatsDto orderStats = orderInternalService.getDashboardOrderStats(start, end);

        // ── Map internal DTO → response DTO ────────────────────────────────
        List<AdminDashboardStatsResponse.DailyChartPoint> dailyChart = orderStats.dailyChart().stream()
            .map(p -> new AdminDashboardStatsResponse.DailyChartPoint(p.date(), p.orderCount(), p.revenue()))
            .collect(Collectors.toList());

        List<AdminDashboardStatsResponse.RecentOrder> recentOrders = orderStats.recentOrders().stream()
            .map(r -> new AdminDashboardStatsResponse.RecentOrder(
                r.id(), r.userId(), r.shopId(), r.status(),
                r.totalAmount(), r.paymentMethod(), r.createdAt()))
            .collect(Collectors.toList());

        AdminDashboardStatsResponse response = new AdminDashboardStatsResponse(
            totalUsers,
            totalShops,
            totalProducts,
            orderStats.totalOrders(),
            orderStats.ordersByStatus(),
            orderStats.deliveredRevenue(),
            orderStats.averageOrderValue(),
            dailyChart,
            recentOrders
        );

        return ApiResponse.<AdminDashboardStatsResponse>builder()
            .status(200)
            .message("Lấy thống kê dashboard thành công")
            .data(response)
            .build();
    }
}
