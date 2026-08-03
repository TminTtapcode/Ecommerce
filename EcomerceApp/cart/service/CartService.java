package com.tmt.ecommerce.cart.service;

import com.tmt.ecommerce.cart.dto.request.CartItemRequest;
import com.tmt.ecommerce.cart.dto.response.CartResponse;

public interface CartService {
    void addToCart(Long userId, CartItemRequest request);
    CartResponse getCart(Long userId);
    void updateItemQuantity(Long userId, Long cartItemId, Integer newQuantity);
    void removeCartItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}

