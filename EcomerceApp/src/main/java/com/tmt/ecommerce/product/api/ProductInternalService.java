package com.tmt.ecommerce.product.api;

import com.tmt.ecommerce.product.api.dto.ProductVariantInfoDto;

public interface ProductInternalService {
    ProductVariantInfoDto getVariantInfo(Long variantId);

    // Thêm hàm trừ kho đồng bộ
    void deductStock(Long variantId, Integer quantity);

    // Methods for Review Module
    Long getProductIdByVariantId(Long variantId);
    Long getShopIdByProductId(Long productId);
}