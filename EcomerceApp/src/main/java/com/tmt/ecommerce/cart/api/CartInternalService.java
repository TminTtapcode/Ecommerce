package com.tmt.ecommerce.cart.api;

import com.tmt.ecommerce.cart.api.dto.CartItemInternalDto;
import java.util.List;

public interface CartInternalService {

    List<CartItemInternalDto> getCartItems(Long userId);

    void clearCart(Long userId);

    void removeCartItems(Long userId, List<Long> cartItemIds);
}
