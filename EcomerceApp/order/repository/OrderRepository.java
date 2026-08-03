package com.tmt.ecommerce.order.repository;

import com.tmt.ecommerce.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Lấy danh sách đơn hàng của một người mua
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Lấy danh sách đơn hàng của một Shop (nhà cung cấp)
    List<Order> findByShopIdOrderByCreatedAtDesc(Long shopId);
}