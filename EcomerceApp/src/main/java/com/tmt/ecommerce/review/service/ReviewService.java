package com.tmt.ecommerce.review.service;

import com.tmt.ecommerce.review.dto.request.ReviewCreateRequest;
import com.tmt.ecommerce.review.dto.request.VendorReplyRequest;
import com.tmt.ecommerce.review.dto.response.RatingSummaryResponse;
import com.tmt.ecommerce.review.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;

public interface ReviewService {
    ReviewResponse createReview(Long userId, ReviewCreateRequest request);
    Page<ReviewResponse> getProductReviews(Long productId, int page, int size);
    RatingSummaryResponse getRatingSummary(Long productId);
    java.util.List<ReviewResponse> getMyReviewsForProduct(Long userId, Long productId);
    ReviewResponse replyReview(Long vendorUserId, Long reviewId, VendorReplyRequest request);

    ReviewResponse updateReview(Long userId, Long reviewId, com.tmt.ecommerce.review.dto.request.ReviewUpdateRequest request);
    void deleteReview(Long userId, Long reviewId);
    java.util.List<Long> checkReviewEligibility(Long userId, Long productId);
}
