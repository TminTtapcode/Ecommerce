package com.tmt.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết ngược lại với sản phẩm gốc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // SKU (Stock Keeping Unit) - Mã phân loại hàng hóa duy nhất
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    // Giá của riêng biến thể này (Có thể cộng/trừ so với giá gốc của Product)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    // Tồn kho riêng cho phân loại này (VD: Size S còn 10 cái, Size M còn 5 cái)
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    // CỘT JSON CHỨA THUỘC TÍNH ĐỘNG (Màu sắc, kích thước, chất liệu...)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "json")
    private Map<String, Object> attributes;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, OUT_OF_STOCK, HIDDEN
}