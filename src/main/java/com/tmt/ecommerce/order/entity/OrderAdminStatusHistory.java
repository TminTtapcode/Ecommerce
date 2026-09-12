package com.tmt.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "order_admin_status_history", indexes = @Index(name = "idx_admin_history_order_id", columnList = "order_id,id"))
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderAdminStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", nullable = false, updatable = false) private Long orderId;
    @Column(name = "actor_user_id", nullable = false, updatable = false) private Long actorUserId;
    @Enumerated(EnumType.STRING) @Column(name = "old_status", nullable = false, updatable = false) private OrderStatus oldStatus;
    @Enumerated(EnumType.STRING) @Column(name = "new_status", nullable = false, updatable = false) private OrderStatus newStatus;
    @Column(nullable = false, length = 500, updatable = false) private String reason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
