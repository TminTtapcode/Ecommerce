package com.tmt.ecommerce.product.api;

import com.tmt.ecommerce.product.api.dto.ProductVariantInfoDto;

import java.util.List;
import java.util.Map;

public interface ProductInternalService {
    ProductVariantInfoDto getVariantInfo(Long variantId);
    Map<Long, ProductVariantInfoDto> getVariantInfos(List<Long> variantIds);

    // Thêm hàm trừ kho đồng bộ
    void deductStock(Long variantId, Integer quantity);

    // Thêm hàm hoàn trả tồn kho khi hủy đơn
    /** Joins a REQUIRED caller transaction (MANDATORY). Invalid input/missing rows fail the whole operation.
     * Caller owns authorization and idempotency; this method deliberately ignores shop/product sale status.
     */
    void restoreStock(Long variantId, Integer quantity);

    // Methods for Review Module
    Long getProductIdByVariantId(Long variantId);
    Map<Long, Long> getProductIdsByVariantIds(List<Long> variantIds);
    Long getShopIdByProductId(Long productId);

    /** Count total products — for Admin Dashboard. */
    long countProducts();
}
