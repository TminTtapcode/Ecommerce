package com.tmt.ecommerce.cart.dto.request;

import java.util.List;

public record CartPreviewRequest(
        List<Long> cartItemIds,
        String voucherCode
) {
}
