package com.tmt.ecommerce.product.service;

import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.product.api.dto.ProductVariantInfoDto;
import com.tmt.ecommerce.product.entity.ProductImage;
import com.tmt.ecommerce.product.entity.ProductVariant;
import com.tmt.ecommerce.product.repository.ProductRepository;
import com.tmt.ecommerce.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductInternalServiceImpl implements ProductInternalService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

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
                variant.getProduct().getShopId(), // Bổ sung tham số shopId bị thiếu vào đây
                variant.getProduct().getName(),
                variant.getSku(),
                variant.getPrice(),
                variant.getStockQuantity(),
                variant.getStatus().name(),
                thumbnailUrl,
                variant.getAttributes());
    }

    @Override
    @Transactional
    public void deductStock(Long variantId, Integer quantity) {
        // 1. Kiểm tra tồn tại
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Biến thể sản phẩm không tồn tại."));

        // 2. Trừ kho biến thể (Product Variant) - Kích hoạt cơ chế Atomic dưới DB
        int variantUpdatedRows = productVariantRepository.deductStock(variantId, quantity);
        if (variantUpdatedRows == 0) {
            // Nếu update trả về 0 dòng ảnh hưởng => Nghĩa là điều kiện (stock >= quantity)
            // bị sai
            throw new IllegalStateException(
                    "Rất tiếc, sản phẩm " + variant.getSku() + " đã hết hàng hoặc không đủ số lượng bạn cần.");
        }

        // 3. Trừ kho gốc (Product) để đồng bộ tổng số lượng
        int productUpdatedRows = productRepository.deductStock(variant.getProduct().getId(), quantity);
        if (productUpdatedRows == 0) {
            throw new IllegalStateException("Lỗi đồng bộ tồn kho gốc của hệ thống.");
        }
    }

    @Override
    public Long getProductIdByVariantId(Long variantId) {
        return productVariantRepository.findById(variantId)
                .map(v -> v.getProduct().getId())
                .orElse(null);
    }

    @Override
    public Long getShopIdByProductId(Long productId) {
        return productRepository.findById(productId)
                .map(p -> p.getShopId())
                .orElse(null);
    }
}