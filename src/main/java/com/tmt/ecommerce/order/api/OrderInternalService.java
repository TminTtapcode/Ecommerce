package com.tmt.ecommerce.order.api;

import com.tmt.ecommerce.order.api.dto.OrderPaymentDto;
import com.tmt.ecommerce.order.api.dto.RefundableOrderSnapshot;
import java.util.List;

public interface OrderInternalService {
    OrderPaymentDto getPaymentDataForGroup(String paymentGroupId);
    void validateGroupOwnershipAndStatus(String paymentGroupId, Long userId);
    /** Lock group orders; confirm only PENDING, preserve all other states. Joins caller transaction. */
    void confirmPendingOrdersByGroup(String paymentGroupId);
    RefundableOrderSnapshot getRefundableOrderSnapshot(long orderId);

    // Methods for Review Module
    boolean isOrderDeliveredAndBelongsToUser(Long orderId, Long userId);
    List<Long> getProductVariantIdsByOrderId(Long orderId);
    List<com.tmt.ecommerce.order.api.dto.DeliveredOrderData> getDeliveredOrdersWithVariantsByUserId(Long userId);

    // Methods for Statistics / Admin Dashboard (F2 / P1-05)
    /** Aggregate order statistics; pass null for startDate/endDate to get all-time stats. */
    com.tmt.ecommerce.order.api.dto.DashboardOrderStatsDto getDashboardOrderStats(
        java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    /**
     * Aggregate a single shop's order snapshots. The caller is responsible for
     * resolving and authorizing the shop from the authenticated principal.
     */
    com.tmt.ecommerce.order.api.dto.ShopOrderAnalyticsDto getShopOrderAnalytics(
        Long shopId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    // Methods for Product Recommendation
    List<Long> getTopSellingVariantIds(int limit);
    List<Long> getRecentlyPurchasedVariantIds(Long userId, int limit);
}
