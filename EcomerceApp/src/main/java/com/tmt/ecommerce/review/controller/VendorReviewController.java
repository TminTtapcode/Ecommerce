package com.tmt.ecommerce.review.controller;

import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.review.dto.request.VendorReplyRequest;
import com.tmt.ecommerce.review.dto.response.ReviewResponse;
import com.tmt.ecommerce.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendor/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
public class VendorReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ApiResponse<ReviewResponse>> replyReview(
            @CurrentUserId Long userId,
            @PathVariable Long reviewId,
            @Valid @RequestBody VendorReplyRequest request) {

        ReviewResponse responseData = reviewService.replyReview(userId, reviewId, request);
        return ResponseEntity.ok(
                ApiResponse.<ReviewResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Phản hồi đánh giá thành công")
                        .data(responseData)
                        .build()
        );
    }
}
