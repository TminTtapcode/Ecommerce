package com.tmt.ecommerce.order.dto.request;

import com.tmt.ecommerce.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record VendorOrderStatusUpdateRequest(
        @NotNull(message = "Trạng thái đơn hàng không được để trống")
        OrderStatus status,
        String reason
) {}
