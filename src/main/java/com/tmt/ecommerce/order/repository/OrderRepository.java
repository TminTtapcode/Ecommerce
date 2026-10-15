package com.tmt.ecommerce.order.repository;

import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status, Pageable pageable);

    Page<Order> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    Page<Order> findByShopIdAndStatusOrderByCreatedAtDesc(Long shopId, OrderStatus status, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    List<Order> findByPaymentGroupId(String paymentGroupId);
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.paymentGroupId = :paymentGroupId ORDER BY o.id")
    List<Order> findByPaymentGroupIdForUpdate(@Param("paymentGroupId") String paymentGroupId);

    @Query(value = "SELECT * FROM orders WHERE status = :#{#status.name()} AND payment_method = :paymentMethod AND created_at <= DATE_SUB(NOW(), INTERVAL 15 MINUTE)", nativeQuery = true)
    List<Order> findExpiredOrders(@Param("status") OrderStatus status, @Param("paymentMethod") String paymentMethod);

    @Query("SELECT o.id AS orderId, i.productVariantId AS productVariantId " +
           "FROM Order o JOIN o.items i " +
           "WHERE o.userId = :userId AND o.status = :status")
    List<OrderVariantProjection> findDeliveredOrderVariants(@Param("userId") Long userId, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate")
    long countByStatusAndCreatedAtBetween(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = com.tmt.ecommerce.order.entity.OrderStatus.DELIVERED AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumDeliveredRevenueByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE', o.createdAt) as date, COUNT(o) as count FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY date ASC")
    List<Object[]> getDailyOrderChartData(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Order> findTop10ByOrderByCreatedAtDesc();

    @Query("""
            SELECT o.status AS status, COUNT(o) AS orderCount
            FROM Order o
            WHERE o.shopId = :shopId
              AND (:startDate IS NULL OR o.createdAt >= :startDate)
              AND (:endDate IS NULL OR o.createdAt <= :endDate)
            GROUP BY o.status
            """)
    List<ShopOrderStatusCountProjection> getShopOrderStatusCounts(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COUNT(o) AS deliveredOrderCount,
                   COALESCE(SUM(o.originalTotalAmount), 0) AS fulfilledGrossOrderValue,
                   COALESCE(SUM(o.discountAmount), 0) AS voucherDiscountAmount,
                   COALESCE(SUM(o.totalAmount), 0) AS fulfilledOrderValue
            FROM Order o
            WHERE o.shopId = :shopId
              AND o.status = com.tmt.ecommerce.order.entity.OrderStatus.DELIVERED
              AND (:startDate IS NULL OR o.createdAt >= :startDate)
              AND (:endDate IS NULL OR o.createdAt <= :endDate)
            """)
    ShopFulfilledOrderValueProjection getShopFulfilledOrderValues(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT FUNCTION('DATE', o.createdAt), COUNT(o),
                   COALESCE(SUM(CASE WHEN o.status = com.tmt.ecommerce.order.entity.OrderStatus.DELIVERED
                                     THEN o.totalAmount ELSE 0 END), 0)
            FROM Order o
            WHERE o.shopId = :shopId
              AND (:startDate IS NULL OR o.createdAt >= :startDate)
              AND (:endDate IS NULL OR o.createdAt <= :endDate)
            GROUP BY FUNCTION('DATE', o.createdAt)
            ORDER BY FUNCTION('DATE', o.createdAt) ASC
            """)
    List<Object[]> getShopDailyOrderAnalytics(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
