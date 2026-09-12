package com.tmt.ecommerce.order.api;

import com.tmt.ecommerce.order.api.dto.OrderPaymentDto;
import com.tmt.ecommerce.order.api.dto.RefundableOrderSnapshot;
import java.util.List;

public interface OrderInternalService {
    OrderPaymentDto getPaymentDataForGroup(String paymentGroupId);
    void validateGroupOwnershipAndStatus(String paymentGroupId, Long userId);

    void confirmPendingOrdersByGroup(String paymentGroupId);
    RefundableOrderSnapshot getRefundableOrderSnapshot(long orderId);

    boolean isOrderDeliveredAndBelongsToUser(Long orderId, Long userId);
    List<Long> getProductVariantIdsByOrderId(Long orderId);
    List<com.tmt.ecommerce.order.api.dto.DeliveredOrderData> getDeliveredOrdersWithVariantsByUserId(Long userId);

    com.tmt.ecommerce.order.api.dto.DashboardOrderStatsDto getDashboardOrderStats(
        java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    com.tmt.ecommerce.order.api.dto.ShopOrderAnalyticsDto getShopOrderAnalytics(
        Long shopId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    List<Long> getTopSellingVariantIds(int limit);
    List<Long> getRecentlyPurchasedVariantIds(Long userId, int limit);
}
