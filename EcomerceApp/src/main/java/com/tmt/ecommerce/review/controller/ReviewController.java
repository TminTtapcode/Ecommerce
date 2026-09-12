package com.tmt.ecommerce.review.controller;

import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.review.dto.request.ReviewCreateRequest;
import com.tmt.ecommerce.review.dto.response.RatingSummaryResponse;
import com.tmt.ecommerce.review.dto.response.ReviewResponse;
import com.tmt.ecommerce.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @CurrentUserId Long userId,
            @Valid @RequestBody ReviewCreateRequest request) {

        ReviewResponse responseData = reviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ReviewResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Đánh giá sản phẩm thành công")
                        .data(responseData)
                        .build()
        );
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReviewResponse> responseData = reviewService.getProductReviews(productId, page, size);
        return ResponseEntity.ok(
                ApiResponse.<Page<ReviewResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách đánh giá thành công")
                        .data(responseData)
                        .build()
        );
    }

    @GetMapping("/products/{productId}/reviews/summary")
    public ResponseEntity<ApiResponse<RatingSummaryResponse>> getRatingSummary(
            @PathVariable Long productId) {

        RatingSummaryResponse responseData = reviewService.getRatingSummary(productId);
        return ResponseEntity.ok(
                ApiResponse.<RatingSummaryResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy thống kê đánh giá thành công")
                        .data(responseData)
                        .build()
        );
    }

    @GetMapping("/products/{productId}/reviews/my-reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<java.util.List<ReviewResponse>>> getMyReviewsForProduct(
            @CurrentUserId Long userId,
            @PathVariable Long productId) {

        java.util.List<ReviewResponse> responseData = reviewService.getMyReviewsForProduct(userId, productId);
        return ResponseEntity.ok(
                ApiResponse.<java.util.List<ReviewResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy đánh giá của tôi thành công")
                        .data(responseData)
                        .build()
        );
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @CurrentUserId Long userId,
            @PathVariable Long reviewId,
            @Valid @RequestBody com.tmt.ecommerce.review.dto.request.ReviewUpdateRequest request) {

        ReviewResponse responseData = reviewService.updateReview(userId, reviewId, request);
        return ResponseEntity.ok(
                ApiResponse.<ReviewResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Cập nhật đánh giá thành công")
                        .data(responseData)
                        .build()
        );
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @CurrentUserId Long userId,
            @PathVariable Long reviewId) {

        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Xóa đánh giá thành công")
                        .build()
        );
    }

    @GetMapping("/products/{productId}/reviews/eligibility")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<java.util.List<Long>>> checkReviewEligibility(
            @CurrentUserId Long userId,
            @PathVariable Long productId) {

        java.util.List<Long> eligibleOrderIds = reviewService.checkReviewEligibility(userId, productId);
        return ResponseEntity.ok(
                ApiResponse.<java.util.List<Long>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Kiểm tra điều kiện đánh giá thành công")
                        .data(eligibleOrderIds)
                        .build()
        );
    }
}
