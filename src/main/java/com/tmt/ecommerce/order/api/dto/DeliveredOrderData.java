package com.tmt.ecommerce.order.api.dto;

import java.util.List;

public record DeliveredOrderData(
    Long orderId,
    List<Long> productVariantIds
) {}
