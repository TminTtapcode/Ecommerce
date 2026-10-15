package com.tmt.ecommerce.payout.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectPayoutRequest {

    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;
}
