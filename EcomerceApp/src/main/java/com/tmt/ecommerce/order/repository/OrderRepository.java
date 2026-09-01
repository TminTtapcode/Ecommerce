package com.tmt.ecommerce.order.repository;

import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Lấy danh sách đơn hàng của một người mua (có phân trang)
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Lấy danh sách đơn hàng của một người mua theo trạng thái (có phân trang)
    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status, Pageable pageable);

    // Lấy danh sách đơn hàng của một Shop (nhà cung cấp) - Cần phân trang sau này
    Page<Order> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    // Lấy danh sách đơn hàng của một Shop theo trạng thái (có phân trang)
    Page<Order> findByShopIdAndStatusOrderByCreatedAtDesc(Long shopId, OrderStatus status, Pageable pageable);

    java.util.List<Order> findByPaymentGroupId(String paymentGroupId);
    java.util.List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Order o SET o.status = :status WHERE o.paymentGroupId = :paymentGroupId")
    void updateStatusByPaymentGroupId(@org.springframework.data.repository.query.Param("paymentGroupId") String paymentGroupId, @org.springframework.data.repository.query.Param("status") OrderStatus status);
}