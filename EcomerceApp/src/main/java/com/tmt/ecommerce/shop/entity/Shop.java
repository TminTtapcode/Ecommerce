package com.tmt.ecommerce.shop.entity;

import jakarta.persistence.*;
import lombok.*;

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

    // Cột này map 1-1 với User (Quy tắc Enterprise: 1 User chỉ có 1 Shop)
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, ACTIVE, BANNED
}