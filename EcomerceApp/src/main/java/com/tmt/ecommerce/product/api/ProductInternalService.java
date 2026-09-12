package com.tmt.ecommerce.product.api;

import com.tmt.ecommerce.product.api.dto.ProductVariantInfoDto;

import java.util.List;
import java.util.Map;

public interface ProductInternalService {
    ProductVariantInfoDto getVariantInfo(Long variantId);
    Map<Long, ProductVariantInfoDto> getVariantInfos(List<Long> variantIds);

    void deductStock(Long variantId, Integer quantity);

    void restoreStock(Long variantId, Integer quantity);

    Long getProductIdByVariantId(Long variantId);
    Map<Long, Long> getProductIdsByVariantIds(List<Long> variantIds);
    Long getShopIdByProductId(Long productId);

    long countProducts();
}
