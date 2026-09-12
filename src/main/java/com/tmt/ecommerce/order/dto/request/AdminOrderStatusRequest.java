package com.tmt.ecommerce.order.dto.request;

import com.tmt.ecommerce.order.entity.OrderStatus;
import jakarta.validation.constraints.*;

public record AdminOrderStatusRequest(@NotNull OrderStatus expectedStatus,
        @NotNull OrderStatus status, @NotBlank @Size(max = 500) String reason) {
    public AdminOrderStatusRequest {
        reason = reason == null ? null : reason.trim();
    }
}
