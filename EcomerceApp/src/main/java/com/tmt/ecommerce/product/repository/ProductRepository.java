package com.tmt.ecommerce.product.repository;

import com.tmt.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopId(Long shopId);

    List<Product> findByNameContainingIgnoreCase(String name);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Filtered by status
    java.util.Optional<Product> findByIdAndStatus(Long id, com.tmt.ecommerce.product.enums.ProductStatus status);

    Page<Product> findByStatus(com.tmt.ecommerce.product.enums.ProductStatus status, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndStatus(String name,
            com.tmt.ecommerce.product.enums.ProductStatus status, Pageable pageable);

    // Kỹ thuật Atomic Update: Trừ tổng kho gốc
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :id AND p.stockQuantity >= :quantity")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}