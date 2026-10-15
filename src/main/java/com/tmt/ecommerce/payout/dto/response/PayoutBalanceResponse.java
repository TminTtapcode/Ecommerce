package com.tmt.ecommerce.payout.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutBalanceResponse {
    private Long shopId;
    private String shopName;
    private long deliveredOrderCount;
    private BigDecimal grossDeliveredRevenue;  // Tổng doanh thu các đơn DELIVERED
    private BigDecimal commissionRate;          // Ví dụ: 0.05 (5%)
    private BigDecimal totalCommission;         // Tiền sàn giữ lại
    private BigDecimal netRevenue;              // Doanh thu thực của shop sau chiết khấu
    private BigDecimal totalWithdrawn;          // Tiền đã rút thành công (APPROVED)
    private BigDecimal pendingWithdrawal;       // Tiền đang chờ duyệt (PENDING)
    private BigDecimal availableBalance;        // Số dư khả dụng có thể rút ngay
}
