package com.tmt.ecommerce.cart.api;

import com.tmt.ecommerce.cart.api.dto.CartItemInternalDto;
import java.util.List;

public interface CartInternalService {
    
    /**
     * Retrieves all items in the user's cart along with their real-time availability and prices.
     */
    List<CartItemInternalDto> getCartItems(Long userId);

    /**
     * Clears all items in the user's cart.
     */
    void clearCart(Long userId);

    /**
     * Removes specific items from the user's cart after checkout.
     */
    void removeCartItems(Long userId, List<Long> cartItemIds);
}
