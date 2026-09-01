package com.tmt.ecommerce.order.service;

import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.dto.request.VendorOrderStatusUpdateRequest;
import com.tmt.ecommerce.order.dto.response.CheckoutResponse;
import com.tmt.ecommerce.order.dto.response.OrderItemResponse;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderItem;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderItemRepository;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.cart.api.CartInternalService;
import com.tmt.ecommerce.cart.api.dto.CartItemInternalDto;

import com.tmt.ecommerce.order.api.OrderInternalService;
import com.tmt.ecommerce.order.api.dto.OrderPaymentDto;
import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.tmt.ecommerce.voucher.api.VoucherInternalService;
import com.tmt.ecommerce.voucher.entity.Voucher;
import com.tmt.ecommerce.voucher.entity.VoucherScope;
import com.tmt.ecommerce.voucher.entity.VoucherType;
import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.cart.dto.request.CartPreviewRequest;
import com.tmt.ecommerce.cart.dto.response.CartPreviewResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService, OrderInternalService {

        private final OrderRepository orderRepository;
        private final CartInternalService cartInternalService;
        private final ProductInternalService productInternalService;
        private final ShopInternalService shopInternalService;
        private final IdentityInternalService identityInternalService;
        private final VoucherInternalService voucherInternalService;

        /**
         * Ma trận chuyển đổi trạng thái hợp lệ cho Vendor.
         * Key: trạng thái hiện tại, Value: danh sách trạng thái có thể chuyển sang.
         */
        private static final Map<OrderStatus, List<OrderStatus>> VENDOR_STATE_TRANSITIONS = Map.of(
                OrderStatus.CONFIRMED, List.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
                OrderStatus.SHIPPED, List.of(OrderStatus.DELIVERED)
        );

        @Override
        @Transactional
        public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
                String paymentGroupId = java.util.UUID.randomUUID().toString();

                // 1. Lấy thông tin giỏ hàng thông qua Internal API
                List<CartItemInternalDto> allCartItems = cartInternalService.getCartItems(userId);
                
                List<CartItemInternalDto> cartItems;
                if (request.cartItemIds() != null && !request.cartItemIds().isEmpty()) {
                        cartItems = allCartItems.stream()
                                .filter(item -> request.cartItemIds().contains(item.cartItemId()))
                                .toList();
                        if (cartItems.isEmpty()) {
                                throw new RuntimeException("Các sản phẩm đã chọn không tồn tại trong giỏ hàng!");
                        }
                } else {
                        cartItems = allCartItems;
                        if (cartItems.isEmpty()) {
                                throw new RuntimeException("Giỏ hàng đang trống, không thể thanh toán!");
                        }
                }

                // 2. LOGIC TÁCH ĐƠN: Dùng cú pháp của Record
                Map<Long, List<CartItemInternalDto>> itemsByShop = cartItems.stream()
                                .collect(Collectors.groupingBy(CartItemInternalDto::shopId));

                // 3. VOUCHER LOGIC (Tự tính toán Subtotal để tránh gian lận)
                Voucher voucher = null;
                if (request.voucherCode() != null && !request.voucherCode().trim().isEmpty()) {
                        voucher = voucherInternalService.validateAndGetVoucher(request.voucherCode());
                        
                        BigDecimal eligibleAmount = BigDecimal.ZERO;
                        if (voucher.getScope() == VoucherScope.SHOP) {
                                // Tự tính subtotal của Shop đó
                                List<CartItemInternalDto> shopItems = itemsByShop.get(voucher.getShopId());
                                if (shopItems == null || shopItems.isEmpty()) {
                                        throw new IllegalArgumentException("Voucher này không áp dụng cho các sản phẩm trong giỏ hàng");
                                }
                                for (CartItemInternalDto item : shopItems) {
                                        eligibleAmount = eligibleAmount.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
                                }
                        } else {
                                // SYSTEM voucher: Tự tính grand total
                                for (CartItemInternalDto item : cartItems) {
                                        eligibleAmount = eligibleAmount.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
                                }
                        }

                        // Kiểm tra minOrderValue
                        if (voucher.getMinOrderValue() != null && eligibleAmount.compareTo(voucher.getMinOrderValue()) < 0) {
                                throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã giảm giá này");
                        }
                        
                        // Increment usage (Atomic Conditional Update), fail fast
                        boolean reserved = voucherInternalService.incrementUsage(voucher.getId());
                        if (!reserved) {
                                throw new IllegalStateException("Mã giảm giá đã hết lượt sử dụng (có người vừa dùng lượt cuối cùng)");
                        }
                }

                List<Order> savedOrders = new ArrayList<>();

                // Tính tổng discount cho System Voucher
                BigDecimal totalCartAmount = BigDecimal.ZERO;
                if (voucher != null && voucher.getScope() == VoucherScope.SYSTEM) {
                        for (CartItemInternalDto item : cartItems) {
                                totalCartAmount = totalCartAmount.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
                        }
                }

                BigDecimal totalSystemDiscount = BigDecimal.ZERO;
                if (voucher != null && voucher.getScope() == VoucherScope.SYSTEM) {
                        if (voucher.getType() == VoucherType.PERCENTAGE) {
                                totalSystemDiscount = totalCartAmount.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
                                if (voucher.getMaxDiscount() != null && totalSystemDiscount.compareTo(voucher.getMaxDiscount()) > 0) {
                                        totalSystemDiscount = voucher.getMaxDiscount();
                                }
                        } else {
                                totalSystemDiscount = voucher.getDiscountValue();
                        }
                }

                // 4. Xử lý từng cụm Shop
                for (Map.Entry<Long, List<CartItemInternalDto>> entry : itemsByShop.entrySet()) {
                        Long shopId = entry.getKey();
                        List<CartItemInternalDto> shopItems = entry.getValue();

                        Order order = Order.builder()
                                        .userId(userId)
                                        .shopId(shopId)
                                        .paymentGroupId(paymentGroupId)
                                        .status(OrderStatus.PENDING)
                                        .shippingAddress(request.shippingAddress())
                                        .paymentMethod(request.paymentMethod())
                                        .originalTotalAmount(BigDecimal.ZERO)
                                        .discountAmount(BigDecimal.ZERO)
                                        .totalAmount(BigDecimal.ZERO)
                                        .build();

                        BigDecimal shopOrderTotal = BigDecimal.ZERO;

                        for (CartItemInternalDto cartItem : shopItems) {
                                BigDecimal subTotal = cartItem.unitPrice().multiply(BigDecimal.valueOf(cartItem.quantity()));
                                shopOrderTotal = shopOrderTotal.add(subTotal);

                                productInternalService.deductStock(cartItem.productVariantId(), cartItem.quantity());

                                OrderItem orderItem = OrderItem.builder()
                                                .productVariantId(cartItem.productVariantId())
                                                .productName(cartItem.productName())
                                                .unitPrice(cartItem.unitPrice())
                                                .quantity(cartItem.quantity())
                                                .subTotal(subTotal)
                                                .build();

                                order.addOrderItem(orderItem);
                        }

                        // Tính discount cho Shop này
                        BigDecimal shopDiscount = BigDecimal.ZERO;
                        if (voucher != null) {
                                if (voucher.getScope() == VoucherScope.SHOP && voucher.getShopId().equals(shopId)) {
                                        if (voucher.getType() == VoucherType.PERCENTAGE) {
                                                shopDiscount = shopOrderTotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
                                                if (voucher.getMaxDiscount() != null && shopDiscount.compareTo(voucher.getMaxDiscount()) > 0) {
                                                        shopDiscount = voucher.getMaxDiscount();
                                                }
                                        } else {
                                                shopDiscount = voucher.getDiscountValue();
                                        }
                                } else if (voucher.getScope() == VoucherScope.SYSTEM && totalCartAmount.compareTo(BigDecimal.ZERO) > 0) {
                                        // Proportional split
                                        // shopDiscount = totalSystemDiscount * (shopOrderTotal / totalCartAmount)
                                        // Dùng phép nhân trước rồi chia để tránh lỗi scale làm tròn sai
                                        shopDiscount = totalSystemDiscount.multiply(shopOrderTotal)
                                                .divide(totalCartAmount, 0, java.math.RoundingMode.HALF_UP);
                                }
                                
                                // Nếu discount lớn hơn tiền hàng thì tối đa discount = tiền hàng
                                if (shopDiscount.compareTo(shopOrderTotal) > 0) {
                                        shopDiscount = shopOrderTotal;
                                }
                                
                                if (shopDiscount.compareTo(BigDecimal.ZERO) > 0) {
                                        order.setAppliedVoucherId(voucher.getId());
                                        order.setAppliedVoucherCode(voucher.getCode());
                                }
                        }

                        order.setOriginalTotalAmount(shopOrderTotal);
                        order.setDiscountAmount(shopDiscount);
                        order.setTotalAmount(shopOrderTotal.subtract(shopDiscount));

                        savedOrders.add(orderRepository.save(order));
                }

                // 5. Dọn dẹp: Xóa các sản phẩm đã thanh toán khỏi giỏ hàng
                List<Long> checkedOutItemIds = cartItems.stream().map(CartItemInternalDto::cartItemId).toList();
                cartInternalService.removeCartItems(userId, checkedOutItemIds);

                // 6. Build DTO trả về
                List<OrderResponse> orderResponses = savedOrders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
                return new CheckoutResponse(paymentGroupId, orderResponses);
        }

        @Override
        @Transactional(readOnly = true)
        public CartPreviewResponse previewCheckout(Long userId, CartPreviewRequest request) {
                // 1. Lấy thông tin giỏ hàng thông qua Internal API
                List<CartItemInternalDto> allCartItems = cartInternalService.getCartItems(userId);
                
                List<CartItemInternalDto> cartItems;
                if (request.cartItemIds() != null && !request.cartItemIds().isEmpty()) {
                        cartItems = allCartItems.stream()
                                .filter(item -> request.cartItemIds().contains(item.cartItemId()))
                                .toList();
                        if (cartItems.isEmpty()) {
                                throw new RuntimeException("Các sản phẩm đã chọn không tồn tại trong giỏ hàng!");
                        }
                } else {
                        cartItems = allCartItems;
                        if (cartItems.isEmpty()) {
                                throw new RuntimeException("Giỏ hàng đang trống, không thể thanh toán!");
                        }
                }

                Map<Long, List<CartItemInternalDto>> itemsByShop = cartItems.stream()
                                .collect(Collectors.groupingBy(CartItemInternalDto::shopId));

                BigDecimal totalCartAmount = BigDecimal.ZERO;
                for (CartItemInternalDto item : cartItems) {
                        totalCartAmount = totalCartAmount.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
                }

                BigDecimal totalDiscount = BigDecimal.ZERO;

                if (request.voucherCode() != null && !request.voucherCode().trim().isEmpty()) {
                        Voucher voucher = voucherInternalService.validateAndGetVoucher(request.voucherCode());
                        
                        BigDecimal eligibleAmount = BigDecimal.ZERO;
                        if (voucher.getScope() == VoucherScope.SHOP) {
                                List<CartItemInternalDto> shopItems = itemsByShop.get(voucher.getShopId());
                                if (shopItems == null || shopItems.isEmpty()) {
                                        throw new IllegalArgumentException("Voucher này không áp dụng cho các sản phẩm trong giỏ hàng");
                                }
                                for (CartItemInternalDto item : shopItems) {
                                        eligibleAmount = eligibleAmount.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
                                }
                        } else {
                                eligibleAmount = totalCartAmount;
                        }

                        if (voucher.getMinOrderValue() != null && eligibleAmount.compareTo(voucher.getMinOrderValue()) < 0) {
                                throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã giảm giá này");
                        }

                        if (voucher.getType() == VoucherType.PERCENTAGE) {
                                totalDiscount = eligibleAmount.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
                                if (voucher.getMaxDiscount() != null && totalDiscount.compareTo(voucher.getMaxDiscount()) > 0) {
                                        totalDiscount = voucher.getMaxDiscount();
                                }
                        } else {
                                totalDiscount = voucher.getDiscountValue();
                        }
                        
                        if (totalDiscount.compareTo(eligibleAmount) > 0) {
                                totalDiscount = eligibleAmount; // Không được giảm quá tiền hàng
                        }
                }

                return CartPreviewResponse.builder()
                        .subtotal(totalCartAmount)
                        .discount(totalDiscount)
                        .total(totalCartAmount.subtract(totalDiscount))
                        .build();
        }

        private OrderResponse mapToOrderResponse(Order order) {
                List<OrderItemResponse> itemResponses = order.getItems().stream()
                                .map(item -> OrderItemResponse.builder()
                                                .id(item.getId())
                                                .productVariantId(item.getProductVariantId())
                                                .productId(productInternalService.getProductIdByVariantId(item.getProductVariantId()))
                                                .productName(item.getProductName())
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .subTotal(item.getSubTotal())
                                                .build())
                                .collect(Collectors.toList());

                return OrderResponse.builder()
                                .id(order.getId())
                                .userId(order.getUserId())
                                .customerName(identityInternalService.getUserFullNameOrDefault(order.getUserId()))
                                .shopId(order.getShopId())
                                .status(order.getStatus().name())
                                .totalAmount(order.getTotalAmount())
                                .shippingAddress(order.getShippingAddress())
                                .paymentMethod(order.getPaymentMethod())
                                .createdAt(order.getCreatedAt())
                                .items(itemResponses)
                                .build();
        }

        @Override
        public Page<OrderResponse> getMyOrders(Long userId, String status, int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<Order> orderPage;

                if (status != null && !status.trim().isEmpty()) {
                        try {
                                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                                orderPage = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, orderStatus, pageable);
                        } catch (IllegalArgumentException e) {
                                // Nếu status không hợp lệ, fallback về lấy tất cả (hoặc throw lỗi 400 Bad Request)
                                orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
                        }
                } else {
                        orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
                }

                return orderPage.map(this::mapToOrderResponse);
        }

        @Override
        public OrderResponse getOrderDetail(Long userId, Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
                
                // Kiểm tra quyền sở hữu đơn hàng
                if (!order.getUserId().equals(userId)) {
                        throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
                }
                
                return mapToOrderResponse(order);
        }

        @Override
        public OrderPaymentDto getPaymentDataForGroup(String paymentGroupId) {
                List<Order> orders = orderRepository.findByPaymentGroupId(paymentGroupId);
                if (orders.isEmpty()) {
                        throw new RuntimeException("Không tìm thấy nhóm đơn hàng");
                }

                BigDecimal totalAmount = orders.stream()
                                .map(Order::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                String method = orders.get(0).getPaymentMethod();

                return new OrderPaymentDto(
                                paymentGroupId,
                                totalAmount,
                                "Thanh toan nhom don hang " + paymentGroupId.substring(0, 8),
                                method);
        }

        @Override
        public void validateGroupOwnershipAndStatus(String paymentGroupId, Long userId) {
                List<Order> orders = orderRepository.findByPaymentGroupId(paymentGroupId);
                if (orders.isEmpty()) {
                        throw new RuntimeException("Không tìm thấy nhóm đơn hàng");
                }

                for (Order order : orders) {
                        if (!order.getUserId().equals(userId)) {
                                throw new RuntimeException("Bạn không có quyền thanh toán nhóm đơn hàng này");
                        }
                        if (order.getStatus() != OrderStatus.PENDING) {
                                throw new RuntimeException("Chỉ đơn hàng ở trạng thái PENDING mới có thể thanh toán");
                        }
                }
        }

        @Override
        @Transactional
        public void updateOrderStatusByGroup(String paymentGroupId, String status) {
                try {
                        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
                        orderRepository.updateStatusByPaymentGroupId(paymentGroupId, newStatus);
                } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Trạng thái đơn hàng không hợp lệ: " + status);
                }
        }

        // === VENDOR ORDER MANAGEMENT ===

        @Override
        public Page<OrderResponse> getVendorOrders(Long userId, String status, int page, int size) {
                Long shopId = shopInternalService.getShopIdByUserId(userId);
                Pageable pageable = PageRequest.of(page, size);
                Page<Order> orderPage;

                if (status != null && !status.trim().isEmpty()) {
                        try {
                                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                                orderPage = orderRepository.findByShopIdAndStatusOrderByCreatedAtDesc(shopId, orderStatus, pageable);
                        } catch (IllegalArgumentException e) {
                                orderPage = orderRepository.findByShopIdOrderByCreatedAtDesc(shopId, pageable);
                        }
                } else {
                        orderPage = orderRepository.findByShopIdOrderByCreatedAtDesc(shopId, pageable);
                }

                return orderPage.map(this::mapToOrderResponse);
        }

        @Override
        public OrderResponse getVendorOrderDetail(Long userId, Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

                if (!shopInternalService.isShopOwner(order.getShopId(), userId)) {
                        throw new RuntimeException("Bạn không có quyền xem đơn hàng này.");
                }

                return mapToOrderResponse(order);
        }

        @Override
        @Transactional
        public OrderResponse updateVendorOrderStatus(Long userId, Long orderId, VendorOrderStatusUpdateRequest request) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

                // Kiểm tra quyền sở hữu Shop
                if (!shopInternalService.isShopOwner(order.getShopId(), userId)) {
                        throw new RuntimeException("Bạn không có quyền cập nhật đơn hàng này.");
                }

                // Kiểm tra tính hợp lệ của chuyển đổi trạng thái
                validateVendorStateTransition(order.getStatus(), request.status());

                log.info("Vendor userId={} cập nhật đơn hàng #{} từ {} sang {}",
                                userId, orderId, order.getStatus(), request.status());

                order.setStatus(request.status());
                Order updatedOrder = orderRepository.save(order);
                return mapToOrderResponse(updatedOrder);
        }

        /**
         * Kiểm tra chuyển đổi trạng thái có hợp lệ theo Ma trận State Machine hay không.
         */
        private void validateVendorStateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
                List<OrderStatus> allowedTransitions = VENDOR_STATE_TRANSITIONS.get(currentStatus);
                if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
                        throw new IllegalArgumentException(
                                String.format("Không thể chuyển trạng thái đơn hàng từ %s sang %s", currentStatus, newStatus)
                        );
                }
        }

        @Override
        public boolean isOrderDeliveredAndBelongsToUser(Long orderId, Long userId) {
                Order order = orderRepository.findById(orderId).orElse(null);
                if (order == null) return false;
                return order.getUserId().equals(userId) && order.getStatus() == OrderStatus.DELIVERED;
        }

        @Override
        public List<Long> getProductVariantIdsByOrderId(Long orderId) {
                Order order = orderRepository.findById(orderId).orElse(null);
                if (order == null) return new ArrayList<>();
                return order.getItems().stream()
                        .map(OrderItem::getProductVariantId)
                        .toList();
        }

        @Override
        public List<Order> getDeliveredOrdersByUserId(Long userId) {
                return orderRepository.findByUserIdAndStatus(userId, OrderStatus.DELIVERED);
        }
}