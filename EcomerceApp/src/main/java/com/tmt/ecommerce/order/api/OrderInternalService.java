package com.tmt.ecommerce.order.api;

import com.tmt.ecommerce.order.api.dto.OrderPaymentDto;
import java.util.List;

public interface OrderInternalService {
    OrderPaymentDto getPaymentDataForGroup(String paymentGroupId);
    void validateGroupOwnershipAndStatus(String paymentGroupId, Long userId);
    void updateOrderStatusByGroup(String paymentGroupId, String status);

    // Methods for Review Module
    boolean isOrderDeliveredAndBelongsToUser(Long orderId, Long userId);
    List<Long> getProductVariantIdsByOrderId(Long orderId);
    List<com.tmt.ecommerce.order.entity.Order> getDeliveredOrdersByUserId(Long userId);
}
