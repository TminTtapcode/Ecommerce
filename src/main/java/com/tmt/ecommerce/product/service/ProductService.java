package com.tmt.ecommerce.product.service;

import com.tmt.ecommerce.product.dto.request.ProductCreateRequest;
import com.tmt.ecommerce.product.dto.request.ProductUpdateRequest;
import com.tmt.ecommerce.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    ProductResponse getVendorProductById(Long userId, Long id);
    ProductResponse createProduct(Long userId, ProductCreateRequest request);

    Page<ProductResponse> getAllProducts(int page, int size, String keyword,
                                        Long categoryId, Long shopId, BigDecimal minPrice, BigDecimal maxPrice,
                                        String sortBy);

    default Page<ProductResponse> getAllProducts(int page, int size, String keyword) {
        return getAllProducts(page, size, keyword, null, null, null, null, null);
    }

    Page<ProductResponse> getVendorProducts(Long userId, int page, int size, String keyword);

    ProductResponse getProductById(Long id);

    Page<ProductResponse> getRecommendedProducts(Long userId, int limit);

    List<ProductResponse> getRelatedProducts(Long productId, Integer limit);

    void deleteProduct(Long userId, Long id);

    ProductResponse updateProduct(Long userId, Long id, ProductUpdateRequest request);
}
