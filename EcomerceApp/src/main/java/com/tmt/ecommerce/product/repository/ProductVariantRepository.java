package com.tmt.ecommerce.product.repository;

import com.tmt.ecommerce.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    // SKU là duy nhất, dùng Optional để tránh NullPointerException
    Optional<ProductVariant> findBySku(String sku);

    // Lấy tất cả biến thể của một sản phẩm cụ thể
    List<ProductVariant> findByProductId(Long productId);
}