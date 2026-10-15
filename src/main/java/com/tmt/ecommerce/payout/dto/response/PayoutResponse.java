package com.tmt.ecommerce.payout.dto.response;

import com.tmt.ecommerce.payout.enums.PayoutStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutResponse {
    private Long id;
    private Long shopId;
    private String shopName;
    private BigDecimal amount;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;
    private PayoutStatus status;
    private String adminNote;
    private Long processedBy;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
