package com.tmt.ecommerce.product.repository;

import com.tmt.ecommerce.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Lấy tất cả các danh mục gốc (Root Category - không có danh mục cha)
    List<Category> findByParentIsNull();
}