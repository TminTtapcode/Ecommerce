package com.tmt.ecommerce.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCreateRequest(
        @NotBlank(message = "Mã nhóm đơn hàng không được để trống")
        String paymentGroupId

) {}