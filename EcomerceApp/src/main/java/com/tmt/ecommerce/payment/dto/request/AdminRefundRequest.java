package com.tmt.ecommerce.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminRefundRequest(
        @NotBlank @Size(max = 500) String reason
) {}
