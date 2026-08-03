package com.tmt.ecommerce.product.repository;

import com.tmt.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Tìm sản phẩm theo gian hàng (Loose coupling với module Shop)
    List<Product> findByShopId(Long shopId);

    // Phục vụ tìm kiếm cơ bản theo tên sản phẩm
    List<Product> findByNameContainingIgnoreCase(String name);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}