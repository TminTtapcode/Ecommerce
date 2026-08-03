package com.tmt.ecommerce.product.service;

import com.tmt.ecommerce.product.dto.request.ProductCreateRequest;
import com.tmt.ecommerce.product.dto.request.ProductUpdateRequest;
import com.tmt.ecommerce.product.dto.response.ProductResponse;
import com.tmt.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request);
    Page<ProductResponse> getAllProducts(int page, int size, String keyword);
    ProductResponse getProductById(Long id);
    void deleteProduct(Long id);
    ProductResponse updateProduct(Long id, ProductUpdateRequest request);
}