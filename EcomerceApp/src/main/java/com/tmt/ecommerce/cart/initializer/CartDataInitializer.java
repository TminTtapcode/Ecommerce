package com.tmt.ecommerce.cart.initializer;

import com.tmt.ecommerce.cart.entity.Cart;
import com.tmt.ecommerce.cart.entity.CartItem;
import com.tmt.ecommerce.cart.repository.CartRepository;
import com.tmt.ecommerce.common.constant.SeedDataIds;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class CartDataInitializer implements CommandLineRunner {

    private final CartRepository cartRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (cartRepository.count() > 0) {
            log.info("Carts already exist. Skipping CartDataInitializer.");
            return;
        }

        log.info("=== STARTING CART SEED DATA ===");

        Cart cart1 = createCart(SeedDataIds.CUSTOMER_1_ID);
        addCartItem(cart1, SeedDataIds.VARIANT_P1_1_ID, 1);
        addCartItem(cart1, SeedDataIds.VARIANT_P3_1_ID, 2);

        Cart cart2 = createCart(SeedDataIds.CUSTOMER_2_ID);
        addCartItem(cart2, SeedDataIds.VARIANT_P2_1_ID, 1);

        log.info("=== FINISHED CART SEED DATA ===");
    }

    private Cart createCart(Long userId) {
        Cart cart = Cart.builder().userId(userId).build();
        return cartRepository.save(cart);
    }

    private void addCartItem(Cart cart, Long variantId, Integer quantity) {
        CartItem item = CartItem.builder().cart(cart).productVariantId(variantId).quantity(quantity).build();
        cart.getItems().add(item);
        cartRepository.save(cart);
    }
}
