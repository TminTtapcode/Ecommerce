package com.tmt.ecommerce.order.repository;

import com.tmt.ecommerce.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.productVariantId FROM OrderItem oi JOIN oi.order o WHERE o.status = 'DELIVERED' GROUP BY oi.productVariantId ORDER BY SUM(oi.quantity) DESC")
    List<Long> findTopSellingVariantIds(Pageable pageable);

    @Query("SELECT oi.productVariantId FROM OrderItem oi JOIN oi.order o WHERE o.userId = :userId GROUP BY oi.productVariantId ORDER BY MAX(o.createdAt) DESC")
    List<Long> findRecentlyPurchasedVariantIds(@Param("userId") Long userId, Pageable pageable);
}
