package com.tmt.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;

// 1. Tạo Entity ProductImage
@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_thumbnail")
    private boolean isThumbnail; // Đánh dấu ảnh bìa

    @Column(name = "sort_order")
    private int sortOrder; // Thứ tự hiển thị ảnh trên UI

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}