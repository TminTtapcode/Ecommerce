package com.tmt.ecommerce.product.service;

import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.product.api.dto.ProductVariantInfoDto;
import com.tmt.ecommerce.product.entity.ProductImage;
import com.tmt.ecommerce.product.entity.ProductVariant;
import com.tmt.ecommerce.product.repository.ProductRepository;
import com.tmt.ecommerce.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductInternalServiceImpl implements ProductInternalService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductVariantInfoDto getVariantInfo(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));

        String thumbnailUrl = variant.getProduct().getImages().stream()
                .filter(ProductImage::isThumbnail)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null);

        return new ProductVariantInfoDto(
                variant.getId(),
                variant.getProduct().getId(),
                variant.getProduct().getShopId(),
                variant.getProduct().getName(),
                variant.getSku(),
                variant.getPrice(),
                variant.getStockQuantity(),
                variant.getStatus().name(),
                thumbnailUrl,
                variant.getAttributes());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ProductVariantInfoDto> getVariantInfos(List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return Map.of();

        List<ProductVariant> variants = productVariantRepository.findByIdIn(variantIds);

        return variants.stream().collect(Collectors.toMap(
                ProductVariant::getId,
                variant -> {
                    String thumbnailUrl = variant.getProduct().getImages().stream()
                            .filter(ProductImage::isThumbnail)
                            .map(ProductImage::getImageUrl)
                            .findFirst()
                            .orElseGet(() -> variant.getProduct().getImages().stream()
                                    .map(ProductImage::getImageUrl)
                                    .findFirst()
                                    .orElse(null));

                    return new ProductVariantInfoDto(
                            variant.getId(),
                            variant.getProduct().getId(),
                            variant.getProduct().getShopId(),
                            variant.getProduct().getName(),
                            variant.getSku(),
                            variant.getPrice(),
                            variant.getStockQuantity(),
                            variant.getStatus().name(),
                            thumbnailUrl,
                            variant.getAttributes());
                },
                (existing, replacement) -> existing
        ));
    }

    @Override
    @Transactional
    public void deductStock(Long variantId, Integer quantity) {

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Biến thể sản phẩm không tồn tại."));

        int variantUpdatedRows = productVariantRepository.deductStock(variantId, quantity);
        if (variantUpdatedRows == 0) {

            throw new IllegalStateException(
                    "Rất tiếc, sản phẩm " + variant.getSku() + " đã hết hàng hoặc không đủ số lượng bạn cần.");
        }

        int productUpdatedRows = productRepository.deductStock(variant.getProduct().getId(), quantity);
        if (productUpdatedRows == 0) {
            throw new IllegalStateException("Lỗi đồng bộ tồn kho gốc của hệ thống.");
        }
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void restoreStock(Long variantId, Integer quantity) {
        if (variantId == null || quantity == null || quantity <= 0) {
            throw new AppException(
                    ErrorCode.INVALID_INPUT);
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.VARIANT_NOT_FOUND));

        if (productVariantRepository.restoreStock(variantId, quantity) != 1) {
            throw new AppException(
                    ErrorCode.INTERNAL_SERVER_ERROR);
        }

        if (variant.getProduct() == null ||
                productRepository.restoreStock(variant.getProduct().getId(), quantity) != 1) {
            throw new AppException(
                    ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getProductIdByVariantId(Long variantId) {
        return productVariantRepository.findById(variantId)
                .map(v -> v.getProduct().getId())
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getProductIdsByVariantIds(List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return Map.of();

        List<ProductVariant> variants = productVariantRepository.findByIdIn(variantIds);
        return variants.stream().collect(Collectors.toMap(
                ProductVariant::getId,
                v -> v.getProduct().getId(),
                (existing, replacement) -> existing
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getShopIdByProductId(Long productId) {
        return productRepository.findById(productId)
                .map(p -> p.getShopId())
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }
}
