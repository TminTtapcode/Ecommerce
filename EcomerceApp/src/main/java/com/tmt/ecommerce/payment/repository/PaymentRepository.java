package com.tmt.ecommerce.payment.repository;

import com.tmt.ecommerce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByPaymentGroupIdAndMethodAndStatus(String paymentGroupId,
            com.tmt.ecommerce.payment.enums.PaymentMethod method,
            com.tmt.ecommerce.payment.enums.PaymentStatus status);

    Optional<Payment> findByTransactionId(String transactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionIdForUpdate(@Param("transactionId") String transactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") Long id);

    List<Payment> findByPaymentGroupIdOrderByCreatedAtDesc(String paymentGroupId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.paymentGroupId = :paymentGroupId " +
            "AND p.method = com.tmt.ecommerce.payment.enums.PaymentMethod.VNPAY " +
            "AND p.status = com.tmt.ecommerce.payment.enums.PaymentStatus.SUCCESS ORDER BY p.id")
    List<Payment> findSuccessfulVnpayByGroupForUpdate(@Param("paymentGroupId") String paymentGroupId);
}
