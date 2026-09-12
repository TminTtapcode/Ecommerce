package com.tmt.ecommerce.cart.repository;

import com.tmt.ecommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"items"})
    Optional<Cart> findByUserId(Long userId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Cart c WHERE c.userId = :userId")
    Optional<Cart> findByUserIdForUpdate(@org.springframework.data.repository.query.Param("userId") Long userId);
}
