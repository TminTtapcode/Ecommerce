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
    // Tìm giao dịch dựa trên mã nội bộ của hệ thống
    Optional<Payment> findByTransactionId(String transactionId);

    // Khóa dòng giao dịch khi xử lý Webhook IPN để tránh xung đột Concurrency
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionIdForUpdate(@Param("transactionId") String transactionId);

    // Lấy tất cả lịch sử thử thanh toán của một nhóm đơn hàng (mới nhất xếp trước)
    List<Payment> findByPaymentGroupIdOrderByCreatedAtDesc(String paymentGroupId);
}