package com.tmt.ecommerce.payout.repository;

import com.tmt.ecommerce.payout.entity.PayoutRequest;
import com.tmt.ecommerce.payout.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface PayoutRequestRepository extends JpaRepository<PayoutRequest, Long> {

    Page<PayoutRequest> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    Page<PayoutRequest> findByStatusOrderByCreatedAtDesc(PayoutStatus status, Pageable pageable);

    Page<PayoutRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PayoutRequest p WHERE p.shopId = :shopId AND p.status = :status")
    BigDecimal sumAmountByShopIdAndStatus(@Param("shopId") Long shopId, @Param("status") PayoutStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PayoutRequest p WHERE p.shopId = :shopId AND p.status IN :statuses")
    BigDecimal sumAmountByShopIdAndStatusIn(@Param("shopId") Long shopId, @Param("statuses") Collection<PayoutStatus> statuses);

    Optional<PayoutRequest> findTopByShopIdOrderByCreatedAtDesc(Long shopId);
}
