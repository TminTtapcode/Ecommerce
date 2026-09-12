package com.tmt.ecommerce.statistics.service;

import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.order.api.OrderInternalService;
import com.tmt.ecommerce.order.api.dto.ShopOrderAnalyticsDto;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import com.tmt.ecommerce.statistics.dto.VendorAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VendorAnalyticsService {
    private final ShopInternalService shopInternalService;
    private final OrderInternalService orderInternalService;

    @Transactional(readOnly = true)
    public VendorAnalyticsResponse getOwnShopAnalytics(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        Long shopId = shopInternalService.findShopIdByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
        LocalDateTime start = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime end = endDate == null ? null : endDate.atTime(java.time.LocalTime.MAX);
        ShopOrderAnalyticsDto data = orderInternalService.getShopOrderAnalytics(shopId, start, end);

        return new VendorAnalyticsResponse(
                "ORDER_CREATED_AT",
                startDate,
                endDate,
                data.totalOrders(),
                data.ordersByStatus(),
                data.deliveredOrderCount(),
                data.fulfilledGrossOrderValue(),
                data.voucherDiscountAmount(),
                data.fulfilledOrderValue(),
                data.averageFulfilledOrderValue(),
                data.dailyChart().stream()
                        .map(point -> new VendorAnalyticsResponse.DailyChartPoint(
                                point.date(), point.createdOrderCount(), point.fulfilledOrderValue()))
                        .toList());
    }
}
