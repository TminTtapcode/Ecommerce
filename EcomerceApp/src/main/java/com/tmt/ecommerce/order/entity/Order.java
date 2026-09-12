package com.tmt.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_status_created", columnList = "user_id, status, created_at"),
    @Index(name = "idx_order_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_order_shop_status_created", columnList = "shop_id, status, created_at"),
    @Index(name = "idx_order_shop_created", columnList = "shop_id, created_at"),
    @Index(name = "idx_order_payment_group", columnList = "payment_group_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "payment_group_id")
    private String paymentGroupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "applied_voucher_id")
    private Long appliedVoucherId;

    @Column(name = "applied_voucher_code")
    private String appliedVoucherCode;

    @Column(name = "original_total_amount", nullable = false)
    @Builder.Default
    private BigDecimal originalTotalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @org.hibernate.annotations.BatchSize(size = 50)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "delivery_confirmed_by_user_id")
    private Long deliveryConfirmedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_confirmation_source")
    private DeliveryConfirmationSource deliveryConfirmationSource;

    public void addOrderItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
