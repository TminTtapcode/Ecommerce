package com.tmt.ecommerce.shop.repository;

import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByUserId(Long userId);

    boolean existsByName(String name);

    boolean existsByIdAndUserId(Long id, Long userId);

    Optional<Shop> findByUserId(Long userId);

    Page<Shop> findByStatus(ShopStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Shop s WHERE s.id = :id")
    Optional<Shop> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT s FROM Shop s WHERE s.id = :id")
    Optional<Shop> findByIdForSale(@Param("id") Long id);

    @Query("SELECT s.id FROM Shop s WHERE s.status = com.tmt.ecommerce.shop.enums.ShopStatus.BANNED")
    List<Long> findBannedIds();
}
