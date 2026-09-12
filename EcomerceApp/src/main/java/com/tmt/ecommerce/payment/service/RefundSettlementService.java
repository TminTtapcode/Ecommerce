package com.tmt.ecommerce.payment.service;

import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.payment.entity.RefundAttempt;
import com.tmt.ecommerce.payment.enums.RefundStatus;
import com.tmt.ecommerce.payment.gateway.RefundGatewayResult;
import com.tmt.ecommerce.payment.repository.RefundAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
class RefundSettlementService {
    private final RefundAttemptRepository refunds;

    @Transactional
    RefundAttempt applyGatewayResult(Long refundId, RefundGatewayResult result) {
        RefundAttempt refund = refunds.findByIdForUpdate(refundId)
                .orElseThrow(() -> new AppException(ErrorCode.REFUND_NOT_FOUND));
        if (refund.getStatus() == RefundStatus.CONFIRMED || refund.getStatus() == RefundStatus.REJECTED) return refund;
        refund.setGatewayResponseCode(result.responseCode());
        refund.setRefundGatewayTxnNo(result.refundTransactionNo());
        refund.setSanitizedGatewayMetadata(result.sanitizedMetadata());
        switch (result.outcome()) {
            case CONFIRMED -> { refund.setStatus(RefundStatus.CONFIRMED); refund.setResolvedAt(LocalDateTime.now()); }
            case REJECTED -> { refund.setStatus(RefundStatus.REJECTED); refund.setResolvedAt(LocalDateTime.now()); }
            case UNKNOWN -> refund.setStatus(RefundStatus.UNKNOWN);
        }
        return refund;
    }
}
