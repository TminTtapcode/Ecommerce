package com.tmt.ecommerce.payment.entity;

import com.tmt.ecommerce.payment.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_attempts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_refund_idempotency_key", columnNames = "idempotency_key"),
        @UniqueConstraint(name = "uk_refund_merchant_request_id", columnNames = "merchant_request_id")
}, indexes = {
        @Index(name = "idx_refund_payment_status", columnList = "payment_id,status"),
        @Index(name = "idx_refund_order_created", columnList = "order_id,created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefundAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    @Column(name = "payment_group_id", nullable = false)
    private String paymentGroupId;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;
    @Column(name = "merchant_request_id", nullable = false, length = 32)
    private String merchantRequestId;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RefundStatus status;
    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;
    @Column(nullable = false, length = 500)
    private String reason;
    @Column(name = "original_txn_ref", nullable = false, length = 100)
    private String originalTxnRef;
    @Column(name = "original_gateway_txn_no", length = 32)
    private String originalGatewayTxnNo;
    @Column(name = "original_gateway_txn_at", length = 14)
    private String originalGatewayTxnAt;
    @Column(name = "refund_gateway_txn_no", length = 32)
    private String refundGatewayTxnNo;
    @Column(name = "gateway_response_code", length = 16)
    private String gatewayResponseCode;
    @Column(name = "sanitized_gateway_metadata", columnDefinition = "TEXT")
    private String sanitizedGatewayMetadata;
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
