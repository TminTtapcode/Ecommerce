package com.tmt.ecommerce.payment.repository;

import com.tmt.ecommerce.payment.entity.RefundAttempt;
import com.tmt.ecommerce.payment.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RefundAttemptRepository extends JpaRepository<RefundAttempt, Long> {
    Optional<RefundAttempt> findByIdempotencyKey(String idempotencyKey);
    Optional<RefundAttempt> findByIdAndOrderId(Long id, Long orderId);
    List<RefundAttempt> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RefundAttempt r where r.id = :id")
    Optional<RefundAttempt> findByIdForUpdate(@Param("id") Long id);

    @Query("select coalesce(sum(r.amount), 0) from RefundAttempt r " +
            "where r.paymentId = :paymentId and r.status in :statuses")
    BigDecimal sumReservedAmountByPaymentId(@Param("paymentId") Long paymentId,
                                             @Param("statuses") Collection<RefundStatus> statuses);

    @Query("select coalesce(sum(r.amount), 0) from RefundAttempt r " +
            "where r.orderId = :orderId and r.status in :statuses")
    BigDecimal sumReservedAmountByOrderId(@Param("orderId") Long orderId,
                                           @Param("statuses") Collection<RefundStatus> statuses);
}
