package com.tmt.ecommerce.review.dto.response;

import java.util.Map;

public record RatingSummaryResponse(
    Double averageRating,
    Long totalReviews,
    Map<Integer, Long> ratingCounts // 5->count, 4->count, 3->count, 2->count, 1->count
) {}
