package com.tmt.ecommerce.product.service;

import com.tmt.ecommerce.product.dto.request.ProductCreateRequest;
import com.tmt.ecommerce.product.dto.request.ProductUpdateRequest;
import com.tmt.ecommerce.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface ProductService {
    ProductResponse getVendorProductById(Long userId, Long id);
    ProductResponse createProduct(Long userId, ProductCreateRequest request);

    /**
     * Tìm kiếm sản phẩm với các filter tùy chọn.
     * D7.1: giá lọc theo product.price. D7.2: categoryId bao gồm descendants.
     * D7.4: sortBy whitelist [price_asc, price_desc, id_desc (default)].
     * Tất cả params đều optional; null/blank bị bỏ qua.
     */
    Page<ProductResponse> getAllProducts(int page, int size, String keyword,
                                        Long categoryId, Long shopId, BigDecimal minPrice, BigDecimal maxPrice,
                                        String sortBy);

    default Page<ProductResponse> getAllProducts(int page, int size, String keyword) {
        return getAllProducts(page, size, keyword, null, null, null, null, null);
    }

    Page<ProductResponse> getVendorProducts(Long userId, int page, int size, String keyword);

    ProductResponse getProductById(Long id);

    /**
     * Hybrid Product Recommendation: Content-Based + Trending
     */
    Page<ProductResponse> getRecommendedProducts(Long userId, int limit);

    /**
     * P2-02 baseline recommendation: public products in the seed's exact
     * category, excluding the seed and catalogue-ineligible products.
     */
    java.util.List<ProductResponse> getRelatedProducts(Long productId, Integer limit);

    void deleteProduct(Long userId, Long id);

    ProductResponse updateProduct(Long userId, Long id, ProductUpdateRequest request);
}
