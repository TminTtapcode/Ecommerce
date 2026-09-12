package com.tmt.ecommerce.product.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.product.dto.response.CategoryResponse;
import com.tmt.ecommerce.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryRepository.findAll().stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.<List<CategoryResponse>>builder()
                .status(200)
                .message("Lấy danh sách danh mục thành công")
                .data(categories)
                .build());
    }
}
