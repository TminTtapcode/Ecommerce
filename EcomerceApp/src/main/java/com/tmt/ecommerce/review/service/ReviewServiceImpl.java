package com.tmt.ecommerce.review.service;

import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.order.api.OrderInternalService;
import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.review.dto.request.ReviewCreateRequest;
import com.tmt.ecommerce.review.dto.request.VendorReplyRequest;
import com.tmt.ecommerce.review.dto.response.RatingSummaryResponse;
import com.tmt.ecommerce.review.dto.response.ReviewResponse;
import com.tmt.ecommerce.review.entity.Review;
import com.tmt.ecommerce.review.repository.ReviewRepository;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderInternalService orderInternalService;
    private final ProductInternalService productInternalService;
    private final ShopInternalService shopInternalService;
    private final IdentityInternalService identityInternalService;

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
        // 1. Kiểm tra đơn hàng thuộc về user và đã giao thành công
        if (!orderInternalService.isOrderDeliveredAndBelongsToUser(request.orderId(), userId)) {
            throw new IllegalArgumentException("Đơn hàng chưa giao thành công hoặc không thuộc về bạn.");
        }

        // 2. Kiểm tra đơn hàng có chứa sản phẩm (thông qua variant)
        List<Long> variantIdsInOrder = orderInternalService.getProductVariantIdsByOrderId(request.orderId());
        boolean hasProduct = variantIdsInOrder.stream()
                .map(productInternalService::getProductIdByVariantId)
                .anyMatch(pid -> pid != null && pid.equals(request.productId()));

        if (!hasProduct) {
            throw new IllegalArgumentException("Sản phẩm không có trong đơn hàng này.");
        }

        // 3. Kiểm tra người dùng đã đánh giá sản phẩm trong đơn hàng này chưa
        if (reviewRepository.existsByUserIdAndOrderIdAndProductId(userId, request.orderId(), request.productId())) {
            throw new IllegalStateException("Bạn đã đánh giá sản phẩm này trong đơn hàng này rồi.");
        }

        // 4. Lưu đánh giá
        Review review = Review.builder()
                .userId(userId)
                .productId(request.productId())
                .orderId(request.orderId())
                .rating(request.rating())
                .comment(request.comment())
                .imageUrls(request.imageUrls() != null ? request.imageUrls() : new java.util.ArrayList<>())
                .build();

        review = reviewRepository.save(review);
        log.info("User {} created review {} for product {}", userId, review.getId(), request.productId());
        return mapToReviewResponse(review);
    }

    @Override
    public Page<ReviewResponse> getProductReviews(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(this::mapToReviewResponse);
    }

    @Override
    public RatingSummaryResponse getRatingSummary(Long productId) {
        Long totalReviews = reviewRepository.countByProductId(productId);
        if (totalReviews == 0) {
            return new RatingSummaryResponse(0.0, 0L, Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L));
        }

        Double avgRatingRaw = reviewRepository.getAverageRatingByProductId(productId);
        Double averageRating = Math.round(avgRatingRaw * 10.0) / 10.0;

        List<Object[]> ratingCountsRaw = reviewRepository.getRatingCountsByProductId(productId);
        Map<Integer, Long> ratingCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingCounts.put(i, 0L);
        }
        for (Object[] row : ratingCountsRaw) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingCounts.put(rating, count);
        }

        return new RatingSummaryResponse(averageRating, totalReviews, ratingCounts);
    }

    @Override
    @Transactional
    public ReviewResponse replyReview(Long vendorUserId, Long reviewId, VendorReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài đánh giá."));

        Long shopId = productInternalService.getShopIdByProductId(review.getProductId());
        if (shopId == null || !shopInternalService.isShopOwner(shopId, vendorUserId)) {
            throw new IllegalArgumentException("Bạn không có quyền phản hồi đánh giá của sản phẩm này.");
        }

        review.setVendorReply(request.replyComment());
        review.setVendorRepliedAt(LocalDateTime.now());
        
        review = reviewRepository.save(review);
        log.info("Vendor {} replied to review {}", vendorUserId, reviewId);
        return mapToReviewResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, com.tmt.ecommerce.review.dto.request.ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài đánh giá."));
        
        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền sửa đánh giá này.");
        }
        
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setImageUrls(request.imageUrls() != null ? request.imageUrls() : new java.util.ArrayList<>());
        
        review = reviewRepository.save(review);
        log.info("User {} updated review {}", userId, reviewId);
        return mapToReviewResponse(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài đánh giá."));
        
        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa đánh giá này.");
        }
        
        reviewRepository.delete(review);
        log.info("User {} deleted review {}", userId, reviewId);
    }

    @Override
    public List<Long> checkReviewEligibility(Long userId, Long productId) {
        // Find delivered orders for this user
        List<com.tmt.ecommerce.order.entity.Order> deliveredOrders = orderInternalService.getDeliveredOrdersByUserId(userId);
        
        List<Long> eligibleOrderIds = new java.util.ArrayList<>();
        
        for (com.tmt.ecommerce.order.entity.Order order : deliveredOrders) {
            // Check if order contains product
            List<Long> variantIdsInOrder = orderInternalService.getProductVariantIdsByOrderId(order.getId());
            boolean hasProduct = variantIdsInOrder.stream()
                    .map(productInternalService::getProductIdByVariantId)
                    .anyMatch(pid -> pid != null && pid.equals(productId));
                    
            if (hasProduct) {
                // Check if already reviewed
                if (!reviewRepository.existsByUserIdAndOrderIdAndProductId(userId, order.getId(), productId)) {
                    eligibleOrderIds.add(order.getId());
                }
            }
        }
        return eligibleOrderIds;
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        String userFullName = identityInternalService.getUserFullNameOrDefault(review.getUserId());
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                userFullName,
                review.getProductId(),
                review.getOrderId(),
                review.getRating(),
                review.getComment(),
                review.getImageUrls(),
                review.getVendorReply(),
                review.getVendorRepliedAt(),
                review.getCreatedAt()
        );
    }
}
