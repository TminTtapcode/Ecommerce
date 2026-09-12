package com.tmt.ecommerce.review.repository;

import com.tmt.ecommerce.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    boolean existsByUserIdAndOrderIdAndProductId(Long userId, Long orderId, Long productId);

    List<Review> findByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT r.orderId FROM Review r WHERE r.userId = :userId AND r.productId = :productId AND r.orderId IN :orderIds")
    List<Long> findReviewedOrderIds(@Param("userId") Long userId, @Param("productId") Long productId, @Param("orderIds") List<Long> orderIds);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId GROUP BY r.rating")
    List<Object[]> getRatingCountsByProductId(@Param("productId") Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId")
    Long countByProductId(@Param("productId") Long productId);
}
