package com.tmt.ecommerce.cart.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartPreviewResponse {
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
}
