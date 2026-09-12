package com.tmt.ecommerce.statistics.controller;

import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.statistics.dto.VendorAnalyticsResponse;
import com.tmt.ecommerce.statistics.service.VendorAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/vendor/analytics")
@RequiredArgsConstructor
public class VendorAnalyticsController {
    private final VendorAnalyticsService vendorAnalyticsService;

    @GetMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ApiResponse<VendorAnalyticsResponse> getOwnShopAnalytics(
            @CurrentUserId Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        VendorAnalyticsResponse data = vendorAnalyticsService.getOwnShopAnalytics(userId, startDate, endDate);
        return ApiResponse.<VendorAnalyticsResponse>builder()
                .status(200)
                .message("Lấy thống kê gian hàng thành công")
                .data(data)
                .build();
    }
}
