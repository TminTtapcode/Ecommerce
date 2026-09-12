package com.tmt.ecommerce.product.repository;

import com.tmt.ecommerce.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNull();

    @Query("SELECT c.id FROM Category c WHERE c.parent.id = :parentId")
    List<Long> findDirectChildIds(@Param("parentId") Long parentId);
}
