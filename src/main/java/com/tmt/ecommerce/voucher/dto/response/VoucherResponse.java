package com.tmt.ecommerce.voucher.dto.response;

import com.tmt.ecommerce.voucher.entity.VoucherScope;
import com.tmt.ecommerce.voucher.entity.VoucherType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VoucherResponse {
    private Long id;
    private String code;
    private VoucherScope scope;
    private Long shopId;
    private VoucherType type;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount;
}
