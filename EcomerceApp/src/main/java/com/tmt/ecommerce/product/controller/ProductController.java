package com.tmt.ecommerce.product.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.product.dto.request.ProductCreateRequest;
import com.tmt.ecommerce.product.dto.request.ProductUpdateRequest;
import com.tmt.ecommerce.product.dto.response.ProductResponse;
import com.tmt.ecommerce.product.entity.Product;
import com.tmt.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        // @Valid sẽ tự động kiểm tra dữ liệu trước khi chạy vào hàm này
        ProductResponse createdProduct = productService.createProduct(request);

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
            @RequestParam(required = false) String keyword) {

        Page<ProductResponse> productPage = productService.getAllProducts(page, size, keyword);

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
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đã ẩn sản phẩm thành công")
                .build();

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductResponse updatedProduct = productService.updateProduct(id, request);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật sản phẩm thành công")
                .data(updatedProduct)
                .build();

        return ResponseEntity.ok(response);
    }
}