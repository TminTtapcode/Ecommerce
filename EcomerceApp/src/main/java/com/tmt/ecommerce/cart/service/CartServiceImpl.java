package com.tmt.ecommerce.cart.service;

import com.tmt.ecommerce.cart.dto.request.CartItemRequest;
import com.tmt.ecommerce.cart.dto.response.CartItemResponse;
import com.tmt.ecommerce.cart.dto.response.CartResponse;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductInternalService productInternalService;
    private final com.tmt.ecommerce.shop.api.ShopInternalService shopInternalService;

    @Override
    @Transactional
    public void addToCart(Long userId, CartItemRequest request) {

        ProductVariantInfoDto variantInfo = productInternalService.getVariantInfo(request.getProductVariantId());

        if (!"ACTIVE".equals(variantInfo.status())) {
            throw new IllegalStateException("Sản phẩm này hiện không kinh doanh.");
        }

        if (variantInfo.stockQuantity() < request.getQuantity()) {
            throw new IllegalStateException("Số lượng tồn kho không đủ. Chỉ còn " + variantInfo.stockQuantity() + " sản phẩm.");
        }

        Cart cart = cartRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });

        shopInternalService.requireNotBannedForSale(variantInfo.shopId());

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductVariantId().equals(request.getProductVariantId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            if (newQuantity > variantInfo.stockQuantity()) {
                throw new IllegalStateException("Tổng số lượng trong giỏ vượt quá tồn kho hiện tại.");
            }
            existingItem.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productVariantId(request.getProductVariantId())
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder().userId(userId).build());

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<CartItemResponse> itemResponses = new ArrayList<>();

        List<Long> variantIds = cart.getItems().stream().map(CartItem::getProductVariantId).toList();
        java.util.Map<Long, ProductVariantInfoDto> variantInfoMap = productInternalService.getVariantInfos(variantIds);

        var bannedShopIds = shopInternalService.getBannedShopIds();
        for (CartItem item : cart.getItems()) {
            ProductVariantInfoDto variantInfo = variantInfoMap.get(item.getProductVariantId());
            if (variantInfo == null) continue;

            BigDecimal subTotal = variantInfo.price().multiply(BigDecimal.valueOf(item.getQuantity()));

            boolean isAvailable = !bannedShopIds.contains(variantInfo.shopId()) && "ACTIVE".equals(variantInfo.status()) && variantInfo.stockQuantity() >= item.getQuantity();

            if (isAvailable) {
                totalAmount = totalAmount.add(subTotal);
            }

            itemResponses.add(new CartItemResponse(
                    item.getId(),
                    variantInfo.variantId(),
                    variantInfo.shopId(),
                    variantInfo.productName(),
                    variantInfo.sku(),
                    variantInfo.price(),
                    item.getQuantity(),
                    subTotal,
                    variantInfo.thumbnailUrl(),
                    variantInfo.attributes(),
                    isAvailable
            ));
        }

        return new CartResponse(cart.getId(), itemResponses, totalAmount);
    }

    @Override
    @Transactional
    public void updateItemQuantity(Long userId, Long cartItemId, Integer newQuantity) {
        Cart cart = cartRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng."));

        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng."));

        if (newQuantity == null || newQuantity < 1) {
            throw new com.tmt.ecommerce.common.exception.AppException(com.tmt.ecommerce.common.exception.ErrorCode.INVALID_INPUT);
        }
        if (newQuantity > itemToUpdate.getQuantity()) {
            ProductVariantInfoDto variantInfo = productInternalService.getVariantInfo(itemToUpdate.getProductVariantId());
            shopInternalService.requireNotBannedForSale(variantInfo.shopId());
            if (newQuantity > variantInfo.stockQuantity()) throw new IllegalStateException("Insufficient stock");
        }

        itemToUpdate.setQuantity(newQuantity);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng."));

        boolean isRemoved = cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        if (!isRemoved) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại trong giỏ hàng.");
        }
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng."));

        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
