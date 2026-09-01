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

    @Override
    @Transactional
    public void addToCart(Long userId, CartItemRequest request) {

        // 1. Giao tiếp lỏng lẻo với module Product thông qua Internal API
        ProductVariantInfoDto variantInfo = productInternalService.getVariantInfo(request.getProductVariantId());

        if (!"ACTIVE".equals(variantInfo.status())) {
            throw new IllegalStateException("Sản phẩm này hiện không kinh doanh.");
        }

        if (variantInfo.stockQuantity() < request.getQuantity()) {
            throw new IllegalStateException("Số lượng tồn kho không đủ. Chỉ còn " + variantInfo.stockQuantity() + " sản phẩm.");
        }

        // 2. Lấy giỏ hàng của User (Nếu chưa có thì tạo mới)
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });

        // 3. Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
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

        // 4. Lưu lại
        cartRepository.save(cart);
    }
    // Trả về toàn bộ thông tin giỏ hàng của User
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder().userId(userId).build()); // Trả về giỏ rỗng nếu chưa có

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Cần import java.util.List và java.util.ArrayList
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            // Lấy data real-time từ module Product
            ProductVariantInfoDto variantInfo = productInternalService.getVariantInfo(item.getProductVariantId());

            // Tính thành tiền của từng món (Dùng hàm .multiply() của BigDecimal)
            BigDecimal subTotal = variantInfo.price().multiply(BigDecimal.valueOf(item.getQuantity()));

            // Kiểm tra trạng thái khả dụng
            boolean isAvailable = "ACTIVE".equals(variantInfo.status()) && variantInfo.stockQuantity() >= item.getQuantity();

            if (isAvailable) {
                totalAmount = totalAmount.add(subTotal);
            }

            itemResponses.add(new CartItemResponse(
                    item.getId(),
                    variantInfo.variantId(),
                    variantInfo.shopId(), // THÊM TRƯỜNG NÀY ĐỂ MAP VỚI RECORD
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
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng."));

        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng."));

        ProductVariantInfoDto variantInfo = productInternalService.getVariantInfo(itemToUpdate.getProductVariantId());
        if (newQuantity > variantInfo.stockQuantity()) {
            throw new IllegalStateException("Số lượng yêu cầu vượt quá tồn kho hiện tại.");
        }

        itemToUpdate.setQuantity(newQuantity);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
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
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng."));

        cart.getItems().clear();
        cartRepository.save(cart);
    }
}