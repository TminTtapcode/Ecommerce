package com.tmt.ecommerce.shop.repository;

import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByUserId(Long userId);
    boolean existsByName(String name);
    boolean existsByIdAndUserId(Long id, Long userId);
    Optional<Shop> findByUserId(Long userId);
    Page<Shop> findByStatus(ShopStatus status, Pageable pageable);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Shop s WHERE s.id = :id")
    Optional<Shop> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_READ)
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Shop s WHERE s.id = :id")
    Optional<Shop> findByIdForSale(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Query("SELECT s.id FROM Shop s WHERE s.status = com.tmt.ecommerce.shop.enums.ShopStatus.BANNED")
    java.util.List<Long> findBannedIds();
}
