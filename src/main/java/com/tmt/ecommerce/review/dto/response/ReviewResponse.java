package com.tmt.ecommerce.review.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReviewResponse(
    Long id,
    Long userId,
    String userFullName,
    Long productId,
    Long orderId,
    Integer rating,
    String comment,
    List<String> imageUrls,
    String vendorReply,
    LocalDateTime vendorRepliedAt,
    LocalDateTime createdAt
) {}
