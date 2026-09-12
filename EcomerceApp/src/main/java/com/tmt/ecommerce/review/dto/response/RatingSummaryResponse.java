package com.tmt.ecommerce.review.dto.response;

import java.util.Map;

public record RatingSummaryResponse(
    Double averageRating,
    Long totalReviews,
    Map<Integer, Long> ratingCounts
) {}
