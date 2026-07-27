package com.tmt.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CHÚ Ý: Loose Coupling với module Shop
    // Chỉ lưu ID để tránh dependency 2 chiều giữa module Product và module Shop
    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Luôn dùng BigDecimal cho tiền tệ, tuyệt đối KHÔNG dùng Double/Float để tránh sai số
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    // Liên kết chặt chẽ (Tightly Coupled) bên trong CÙNG một module Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, HIDDEN, OUT_OF_STOCK, BANNED
}