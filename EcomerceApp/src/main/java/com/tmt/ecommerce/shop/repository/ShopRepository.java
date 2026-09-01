package com.tmt.ecommerce.shop.repository;

import com.tmt.ecommerce.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByUserId(Long userId);
    boolean existsByName(String name);
    boolean existsByIdAndUserId(Long id, Long userId);
    Optional<Shop> findByUserId(Long userId);
}