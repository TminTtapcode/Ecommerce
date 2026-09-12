package com.tmt.ecommerce.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CheckoutRequest(
        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        String shippingAddress,

        @NotBlank(message = "Phương thức thanh toán không được để trống")
        String paymentMethod,

        String voucherCode,

        java.util.List<Long> cartItemIds
) {}
