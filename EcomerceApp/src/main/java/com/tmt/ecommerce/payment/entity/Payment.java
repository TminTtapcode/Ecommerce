package com.tmt.ecommerce.payment.entity;

import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_group_id", nullable = false)
    private String paymentGroupId;

    @Column(name = "order_id", nullable = true)
    private Long orderId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method; // VNPAY, MOMO, COD

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId; // Mã giao dịch nội bộ sinh ra (VD: PAY_123456)

    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId; // Mã giao dịch phía đối tác (VD: Mã chuẩn chi của VNPAY)

    @Column(columnDefinition = "TEXT")
    private String rawData; // Cực kỳ quan trọng: Lưu lại toàn bộ response của VNPAY để đối soát

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}