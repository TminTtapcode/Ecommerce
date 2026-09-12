package com.tmt.ecommerce.product.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.product.dto.request.ProductCreateRequest;
import com.tmt.ecommerce.product.dto.request.ProductUpdateRequest;
import com.tmt.ecommerce.product.dto.response.ProductResponse;
import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.product.entity.Product;
import com.tmt.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @CurrentUserId Long userId,
            @Valid @RequestBody ProductCreateRequest request) {
        // @Valid sẽ tự động kiểm tra dữ liệu trước khi chạy vào hàm này
        ProductResponse createdProduct = productService.createProduct(userId, request);

        // Bọc dữ liệu vào ApiResponse để đồng bộ với các API khác
        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo mới sản phẩm thành công")
                .data(createdProduct)
                .build();

        // Trả về HTTP Status 201 (Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy) {

        // D7.5: reject nếu min > max (cả hai đều được cung cấp)
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            return ResponseEntity.badRequest().body(
                ApiResponse.<Page<ProductResponse>>builder()
                    .status(400)
                    .message("minPrice không được lớn hơn maxPrice")
                    .build()
            );
        }

        Page<ProductResponse> productPage = productService.getAllProducts(
                page, size, keyword, categoryId, shopId, minPrice, maxPrice, sortBy);

        ApiResponse<Page<ProductResponse>> response = ApiResponse.<Page<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách sản phẩm thành công")
                .data(productPage)
                .build();

        return ResponseEntity.ok(response);
    }

    // API Lấy chi tiết 1 sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin sản phẩm thành công")
                .data(product)
                .build();

        return ResponseEntity.ok(response);
    }

    /** P2-02: public, exact-category related products. */
    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<java.util.List<ProductResponse>>> getRelatedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "8") Integer limit) {
        java.util.List<ProductResponse> related = productService.getRelatedProducts(id, limit);
        return ResponseEntity.ok(ApiResponse.<java.util.List<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy sản phẩm liên quan thành công")
                .data(related)
                .build());
    }

    /**
     * P2-04: Hybrid Product Recommendation
     */
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getRecommendations(
            @CurrentUserId(required = false) Long userId,
            @RequestParam(defaultValue = "12") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(50) Integer limit) {
        
        Page<ProductResponse> recommendationPage = productService.getRecommendedProducts(userId, limit);
        
        return ResponseEntity.ok(ApiResponse.<Page<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách gợi ý sản phẩm thành công")
                .data(recommendationPage)
                .build());
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @CurrentUserId Long userId,
            @PathVariable Long id) {
        productService.deleteProduct(userId, id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã ẩn sản phẩm thành công")
                .build();

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @CurrentUserId Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductResponse updatedProduct = productService.updateProduct(userId, id, request);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật sản phẩm thành công")
                .data(updatedProduct)
                .build();

        return ResponseEntity.ok(response);
    }
}
