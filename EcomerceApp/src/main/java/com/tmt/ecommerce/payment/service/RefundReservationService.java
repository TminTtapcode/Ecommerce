package com.tmt.ecommerce.payment.service;

import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.order.api.dto.RefundableOrderSnapshot;
import com.tmt.ecommerce.payment.entity.Payment;
import com.tmt.ecommerce.payment.entity.RefundAttempt;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.enums.RefundStatus;
import com.tmt.ecommerce.payment.repository.PaymentRepository;
import com.tmt.ecommerce.payment.repository.RefundAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class RefundReservationService {
    private static final EnumSet<RefundStatus> RESERVED = EnumSet.of(RefundStatus.REQUESTED, RefundStatus.UNKNOWN, RefundStatus.CONFIRMED);
    private final PaymentRepository payments;
    private final RefundAttemptRepository refunds;

    @Transactional
    RefundAttempt reserve(RefundableOrderSnapshot order, Long actorId, String idempotencyKey, String reason) {
        var replay = refunds.findByIdempotencyKey(idempotencyKey);
        if (replay.isPresent()) {
            RefundAttempt existing = replay.get();
            if (!existing.getOrderId().equals(order.orderId()) || !existing.getActorUserId().equals(actorId)
                    || !existing.getReason().equals(reason)) throw new AppException(ErrorCode.REFUND_IDEMPOTENCY_CONFLICT);
            return existing;
        }
        List<Payment> candidates = payments.findSuccessfulVnpayByGroupForUpdate(order.paymentGroupId());

        replay = refunds.findByIdempotencyKey(idempotencyKey);
        if (replay.isPresent()) {
            RefundAttempt existing = replay.get();
            if (!existing.getOrderId().equals(order.orderId()) || !existing.getActorUserId().equals(actorId)
                    || !existing.getReason().equals(reason)) throw new AppException(ErrorCode.REFUND_IDEMPOTENCY_CONFLICT);
            return existing;
        }
        if (candidates.size() != 1) throw new AppException(ErrorCode.REFUND_NOT_ACTIONABLE);
        Payment payment = candidates.getFirst();
        if (blank(payment.getTransactionId()) || blank(payment.getGatewayTransactionId()) || blank(payment.getVnpayTransactionDate()))
            throw new AppException(ErrorCode.REFUND_NOT_ACTIONABLE);

        BigDecimal orderReserved = refunds.sumReservedAmountByOrderId(order.orderId(), RESERVED);
        BigDecimal amount = order.totalAmount().subtract(orderReserved);
        if (amount.signum() <= 0) throw new AppException(ErrorCode.REFUND_NOT_ACTIONABLE);
        BigDecimal paymentReserved = refunds.sumReservedAmountByPaymentId(payment.getId(), RESERVED);
        if (paymentReserved.add(amount).compareTo(payment.getAmount()) > 0) throw new AppException(ErrorCode.REFUND_NOT_ACTIONABLE);

        return refunds.save(RefundAttempt.builder().orderId(order.orderId()).paymentId(payment.getId())
                .paymentGroupId(order.paymentGroupId()).amount(amount).currency("VND").idempotencyKey(idempotencyKey)
                .merchantRequestId("R" + UUID.randomUUID().toString().replace("-", "").substring(0, 31))
                .status(RefundStatus.REQUESTED).actorUserId(actorId).reason(reason).originalTxnRef(payment.getTransactionId())
                .originalGatewayTxnNo(payment.getGatewayTransactionId()).originalGatewayTxnAt(payment.getVnpayTransactionDate())
                .requestedAt(LocalDateTime.now()).build());
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
