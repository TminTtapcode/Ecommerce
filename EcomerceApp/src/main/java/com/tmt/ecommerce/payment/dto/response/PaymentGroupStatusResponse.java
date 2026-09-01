package com.tmt.ecommerce.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGroupStatusResponse {
    private String paymentGroupId;
    private String status; // PENDING, SUCCESS, FAILED
}
