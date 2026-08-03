package com.tmt.ecommerce.product.api;

import com.tmt.ecommerce.product.api.dto.ProductVariantInfoDto;

public interface ProductInternalService {
    // Chỉ trả về DTO, tuyệt đối không trả về Entity ProductVariant
    ProductVariantInfoDto getVariantInfo(Long variantId);
}