package com.tmt.ecommerce.product.service;

import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.product.api.dto.ProductVariantInfoDto;
import com.tmt.ecommerce.product.entity.ProductImage;
import com.tmt.ecommerce.product.entity.ProductVariant;
import com.tmt.ecommerce.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductInternalServiceImpl implements ProductInternalService {

    private final ProductVariantRepository productVariantRepository;

    @Override
    public ProductVariantInfoDto getVariantInfo(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));

        // Tìm ảnh thumbnail của sản phẩm gốc
        String thumbnailUrl = variant.getProduct().getImages().stream()
                .filter(ProductImage::isThumbnail)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null); // Nếu không có ảnh bìa thì trả về null

        return new ProductVariantInfoDto(
                variant.getId(),
                variant.getProduct().getName(),
                variant.getSku(),
                variant.getPrice(),
                variant.getStockQuantity(),
                variant.getStatus(),
                thumbnailUrl,
                variant.getAttributes()
        );
    }
}