package com.tmt.ecommerce.voucher.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // e.g. "SUMMER2026"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherScope scope; // SYSTEM, SHOP

    @Column(name = "shop_id")
    private Long shopId; // Null if scope == SYSTEM

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType type; // PERCENTAGE, FIXED_AMOUNT

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue; // e.g., 10 (for 10%), or 20000 (VND)

    @Column(name = "max_discount")
    private BigDecimal maxDiscount; // Max cap for PERCENTAGE

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue; // Minimum spend required

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
