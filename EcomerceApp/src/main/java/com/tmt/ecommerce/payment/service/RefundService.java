package com.tmt.ecommerce.payment.service;

import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.order.api.OrderInternalService;
import com.tmt.ecommerce.order.api.dto.RefundableOrderSnapshot;
import com.tmt.ecommerce.payment.dto.response.RefundAttemptResponse;
import com.tmt.ecommerce.payment.entity.RefundAttempt;
import com.tmt.ecommerce.payment.enums.RefundStatus;
import com.tmt.ecommerce.payment.gateway.PaymentRefundGateway;
import com.tmt.ecommerce.payment.gateway.RefundGatewayRequest;
import com.tmt.ecommerce.payment.repository.RefundAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundService {
    private final OrderInternalService orders;
    private final RefundAttemptRepository refunds;
    private final RefundReservationService reservations;
    private final RefundSettlementService settlements;
    private final PaymentRefundGateway gateway;
    @Value("${payment.vnpay.api-ip}") private String apiIp;

    public RefundAttemptResponse create(Long orderId, Long actorId, String idempotencyKey, String reason) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 128)
            throw new AppException(ErrorCode.INVALID_INPUT);
        requireGatewayIp();
        RefundableOrderSnapshot order = orders.getRefundableOrderSnapshot(orderId);
        if (!"CANCELLED".equals(order.status()) || !"VNPAY".equalsIgnoreCase(order.paymentMethod())
                || order.paymentGroupId() == null || order.totalAmount() == null || order.totalAmount().signum() <= 0)
            throw new AppException(ErrorCode.REFUND_NOT_ACTIONABLE);
        RefundAttempt reserved = reservations.reserve(order, actorId, idempotencyKey, reason);
        if (reserved.getStatus() != RefundStatus.REQUESTED) return RefundAttemptResponse.from(reserved);
        return RefundAttemptResponse.from(settlements.applyGatewayResult(reserved.getId(), gateway.refund(requestFor(reserved))));
    }

    @Transactional(readOnly = true)
    public List<RefundAttemptResponse> listForOrder(Long orderId) {

        return refunds.findByOrderIdOrderByCreatedAtDesc(orderId).stream().map(RefundAttemptResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RefundAttemptResponse get(Long refundId) {
        return RefundAttemptResponse.from(refunds.findById(refundId)
                .orElseThrow(() -> new AppException(ErrorCode.REFUND_NOT_FOUND)));
    }

    public RefundAttemptResponse reconcile(Long refundId) {
        requireGatewayIp();
        RefundAttempt refund = refunds.findById(refundId).orElseThrow(() -> new AppException(ErrorCode.REFUND_NOT_FOUND));
        if (refund.getStatus() == RefundStatus.CONFIRMED || refund.getStatus() == RefundStatus.REJECTED)
            return RefundAttemptResponse.from(refund);
        RefundGatewayRequest query = new RefundGatewayRequest("Q" + UUID.randomUUID().toString().replace("-", "").substring(0, 31),
                refund.getOriginalTxnRef(), refund.getOriginalGatewayTxnNo(), refund.getOriginalGatewayTxnAt(), refund.getAmount(),
                "ADMIN" + refund.getActorUserId(), apiIp, "Refund order " + refund.getOrderId(), false);
        return RefundAttemptResponse.from(settlements.applyGatewayResult(refundId, gateway.query(query)));
    }

    private RefundGatewayRequest requestFor(RefundAttempt refund) {
        return new RefundGatewayRequest(refund.getMerchantRequestId(), refund.getOriginalTxnRef(), refund.getOriginalGatewayTxnNo(),
                refund.getOriginalGatewayTxnAt(), refund.getAmount(), "ADMIN" + refund.getActorUserId(), apiIp,
                "Refund order " + refund.getOrderId(), false);
    }

    private void requireGatewayIp() {
        if (apiIp == null || apiIp.isBlank()) throw new AppException(ErrorCode.REFUND_NOT_ACTIONABLE);
    }
}
