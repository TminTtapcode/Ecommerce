package com.tmt.ecommerce.product.repository;

import com.tmt.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Product> {
    List<Product> findByShopId(Long shopId);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByShopId(Long shopId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByShopIdAndNameContainingIgnoreCase(Long shopId, String name, Pageable pageable);

    List<Product> findByNameContainingIgnoreCase(String name);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    java.util.Optional<Product> findByIdAndStatus(Long id, com.tmt.ecommerce.product.enums.ProductStatus status);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByStatus(com.tmt.ecommerce.product.enums.ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByNameContainingIgnoreCaseAndStatus(String name,
            com.tmt.ecommerce.product.enums.ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findByCategory_IdAndStatusAndIdNot(Long categoryId,
            com.tmt.ecommerce.product.enums.ProductStatus status, Long excludedProductId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findByCategory_IdAndStatusAndIdNotAndShopIdNotIn(Long categoryId,
            com.tmt.ecommerce.product.enums.ProductStatus status, Long excludedProductId,
            List<Long> excludedShopIds, Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :id AND p.stockQuantity >= :quantity")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :id")
    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds AND p.id NOT IN :excludedIds AND p.status = :status AND p.shopId NOT IN :bannedShopIds ORDER BY p.id DESC")
    List<Product> findByCategoryIdsExcluding(@Param("categoryIds") List<Long> categoryIds,
            @Param("excludedIds") List<Long> excludedIds, @Param("status") com.tmt.ecommerce.product.enums.ProductStatus status,
            @Param("bannedShopIds") List<Long> bannedShopIds, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds AND p.id NOT IN :excludedIds AND p.status = :status ORDER BY p.id DESC")
    List<Product> findByCategoryIdsExcluding(@Param("categoryIds") List<Long> categoryIds,
            @Param("excludedIds") List<Long> excludedIds, @Param("status") com.tmt.ecommerce.product.enums.ProductStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.id NOT IN :excludedIds AND p.shopId NOT IN :bannedShopIds ORDER BY p.id DESC")
    List<Product> findNewestExcluding(@Param("excludedIds") List<Long> excludedIds,
            @Param("status") com.tmt.ecommerce.product.enums.ProductStatus status,
            @Param("bannedShopIds") List<Long> bannedShopIds, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.id NOT IN :excludedIds ORDER BY p.id DESC")
    List<Product> findNewestExcluding(@Param("excludedIds") List<Long> excludedIds,
            @Param("status") com.tmt.ecommerce.product.enums.ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.status = :status AND p.shopId NOT IN :bannedShopIds")
    List<Product> findByIdsAndStatus(@Param("ids") List<Long> ids, @Param("status") com.tmt.ecommerce.product.enums.ProductStatus status, @Param("bannedShopIds") List<Long> bannedShopIds);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.status = :status")
    List<Product> findByIdsAndStatus(@Param("ids") List<Long> ids, @Param("status") com.tmt.ecommerce.product.enums.ProductStatus status);
}
