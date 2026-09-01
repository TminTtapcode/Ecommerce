package com.tmt.ecommerce.cart.repository;

import com.tmt.ecommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Tìm giỏ hàng hiện tại của User
    Optional<Cart> findByUserId(Long userId);
}