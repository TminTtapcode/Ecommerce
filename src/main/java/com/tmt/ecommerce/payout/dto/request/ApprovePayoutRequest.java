package com.tmt.ecommerce.payout.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovePayoutRequest {
    private String adminNote;
}
