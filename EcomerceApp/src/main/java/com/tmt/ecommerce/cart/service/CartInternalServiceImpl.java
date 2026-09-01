package com.tmt.ecommerce.cart.service;

import com.tmt.ecommerce.cart.api.CartInternalService;
import com.tmt.ecommerce.cart.api.dto.CartItemInternalDto;
import com.tmt.ecommerce.cart.entity.Cart;
import com.tmt.ecommerce.cart.entity.CartItem;
import com.tmt.ecommerce.cart.repository.CartRepository;
import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.product.api.dto.ProductVariantInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartInternalServiceImpl implements CartInternalService {

    private final CartRepository cartRepository;
    private final ProductInternalService productInternalService;

    @Override
    @Transactional(readOnly = true)
    public List<CartItemInternalDto> getCartItems(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder().userId(userId).build()); // Empty cart

        List<CartItemInternalDto> internalDtos = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            ProductVariantInfoDto variantInfo = productInternalService.getVariantInfo(item.getProductVariantId());
            
            BigDecimal subTotal = variantInfo.price().multiply(BigDecimal.valueOf(item.getQuantity()));
            boolean isAvailable = "ACTIVE".equals(variantInfo.status()) && variantInfo.stockQuantity() >= item.getQuantity();

            internalDtos.add(new CartItemInternalDto(
                    item.getId(),
                    variantInfo.variantId(),
                    variantInfo.shopId(),
                    variantInfo.productName(),
                    variantInfo.price(),
                    item.getQuantity(),
                    subTotal,
                    isAvailable
            ));
        }

        return internalDtos;
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    @Override
    @Transactional
    public void removeCartItems(Long userId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return;
        }
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().removeIf(item -> cartItemIds.contains(item.getId()));
            cartRepository.save(cart);
        });
    }
}
