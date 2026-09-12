package com.tmt.ecommerce.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import com.tmt.ecommerce.shop.enums.ShopStatus;

@Entity
@Table(name = "shops")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShopStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "prior_status", length = 20)
    private ShopStatus priorStatus;
}
