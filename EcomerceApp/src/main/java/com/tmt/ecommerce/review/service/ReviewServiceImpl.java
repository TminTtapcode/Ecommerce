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
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {

        if (!orderInternalService.isOrderDeliveredAndBelongsToUser(request.orderId(), userId)) {
            throw new IllegalArgumentException("Đơn hàng chưa giao thành công hoặc không thuộc về bạn.");
        }

        List<Long> variantIdsInOrder = orderInternalService.getProductVariantIdsByOrderId(request.orderId());
        boolean hasProduct = variantIdsInOrder.stream()
                .map(productInternalService::getProductIdByVariantId)
                .anyMatch(pid -> pid != null && pid.equals(request.productId()));

        if (!hasProduct) {
            throw new IllegalArgumentException("Sản phẩm không có trong đơn hàng này.");
        }

        if (reviewRepository.existsByUserIdAndOrderIdAndProductId(userId, request.orderId(), request.productId())) {
            throw new IllegalStateException("Bạn đã đánh giá sản phẩm này trong đơn hàng này rồi.");
        }

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

        Long shopId = productInternalService.getShopIdByProductId(request.productId());
        eventPublisher.publishEvent(new com.tmt.ecommerce.review.api.event.ReviewCreatedEvent(
                review.getId(), userId, shopId, request.productId(), request.rating()
        ));

        return mapToReviewResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(this::mapToReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public java.util.List<ReviewResponse> getMyReviewsForProduct(Long userId, Long productId) {
        List<Review> myReviews = reviewRepository.findByUserIdAndProductId(userId, productId);
        return myReviews.stream()
                .map(this::mapToReviewResponse)
                .toList();
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
    @Transactional(readOnly = true)
    public List<Long> checkReviewEligibility(Long userId, Long productId) {

        List<com.tmt.ecommerce.order.api.dto.DeliveredOrderData> deliveredOrders =
                orderInternalService.getDeliveredOrdersWithVariantsByUserId(userId);

        List<Long> orderIdsWithProduct = new java.util.ArrayList<>();

        List<Long> allVariantIds = deliveredOrders.stream()
                .flatMap(orderData -> orderData.productVariantIds().stream())
                .distinct()
                .toList();

        Map<Long, Long> variantToProductMap = productInternalService.getProductIdsByVariantIds(allVariantIds);

        for (com.tmt.ecommerce.order.api.dto.DeliveredOrderData orderData : deliveredOrders) {

            List<Long> variantIdsInOrder = orderData.productVariantIds();
            boolean hasProduct = variantIdsInOrder.stream()
                    .map(variantToProductMap::get)
                    .anyMatch(pid -> pid != null && pid.equals(productId));

            if (hasProduct) {
                orderIdsWithProduct.add(orderData.orderId());
            }
        }

        if (orderIdsWithProduct.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        List<Long> reviewedOrderIds = reviewRepository.findReviewedOrderIds(userId, productId, orderIdsWithProduct);

        orderIdsWithProduct.removeAll(reviewedOrderIds);

        return orderIdsWithProduct;
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
