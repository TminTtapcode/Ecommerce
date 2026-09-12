package com.tmt.ecommerce.order.repository;

import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o WHERE o.id = :id")
    java.util.Optional<Order> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status, Pageable pageable);

    Page<Order> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    Page<Order> findByShopIdAndStatusOrderByCreatedAtDesc(Long shopId, OrderStatus status, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    java.util.List<Order> findByPaymentGroupId(String paymentGroupId);
    java.util.List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o WHERE o.paymentGroupId = :paymentGroupId ORDER BY o.id")
    java.util.List<Order> findByPaymentGroupIdForUpdate(@org.springframework.data.repository.query.Param("paymentGroupId") String paymentGroupId);

    @org.springframework.data.jpa.repository.Query("SELECT o.id AS orderId, i.productVariantId AS productVariantId " +
           "FROM Order o JOIN o.items i " +
           "WHERE o.userId = :userId AND o.status = :status")
    java.util.List<OrderVariantProjection> findDeliveredOrderVariants(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("status") OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate")
    long countByStatusAndCreatedAtBetween(@org.springframework.data.repository.query.Param("status") OrderStatus status, @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = com.tmt.ecommerce.order.entity.OrderStatus.DELIVERED AND o.createdAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumDeliveredRevenueByCreatedAtBetween(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("SELECT FUNCTION('DATE', o.createdAt) as date, COUNT(o) as count FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY date ASC")
    java.util.List<Object[]> getDailyOrderChartData(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    java.util.List<Order> findTop10ByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Query("""
            SELECT o.status AS status, COUNT(o) AS orderCount
            FROM Order o
            WHERE o.shopId = :shopId
              AND (:startDate IS NULL OR o.createdAt >= :startDate)
              AND (:endDate IS NULL OR o.createdAt <= :endDate)
            GROUP BY o.status
            """)
    java.util.List<ShopOrderStatusCountProjection> getShopOrderStatusCounts(
            @org.springframework.data.repository.query.Param("shopId") Long shopId,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("""
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
            @org.springframework.data.repository.query.Param("shopId") Long shopId,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("""
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
    java.util.List<Object[]> getShopDailyOrderAnalytics(
            @org.springframework.data.repository.query.Param("shopId") Long shopId,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);
}
