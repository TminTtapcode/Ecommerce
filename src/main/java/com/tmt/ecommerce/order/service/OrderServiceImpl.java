package com.tmt.ecommerce.order.service;

import com.tmt.ecommerce.cart.api.CartInternalService;
import com.tmt.ecommerce.cart.api.dto.CartItemInternalDto;
import com.tmt.ecommerce.cart.dto.request.CartPreviewRequest;
import com.tmt.ecommerce.cart.dto.response.CartPreviewResponse;
import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.BusinessException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.common.exception.ResourceNotFoundException;
import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.order.api.OrderInternalService;
import com.tmt.ecommerce.order.api.dto.DashboardOrderStatsDto;
import com.tmt.ecommerce.order.api.dto.DeliveredOrderData;
import com.tmt.ecommerce.order.api.dto.OrderItemCancelDto;
import com.tmt.ecommerce.order.api.dto.OrderPaymentDto;
import com.tmt.ecommerce.order.api.dto.RefundableOrderSnapshot;
import com.tmt.ecommerce.order.api.dto.ShopOrderAnalyticsDto;
import com.tmt.ecommerce.order.api.event.OrderCancelledEvent;
import com.tmt.ecommerce.order.api.event.OrderConfirmedEvent;
import com.tmt.ecommerce.order.api.event.OrderDeliveredEvent;
import com.tmt.ecommerce.order.api.event.OrderShippedEvent;
import com.tmt.ecommerce.order.dto.request.CheckoutRequest;
import com.tmt.ecommerce.order.dto.request.VendorOrderStatusUpdateRequest;
import com.tmt.ecommerce.order.dto.response.CheckoutResponse;
import com.tmt.ecommerce.order.dto.response.OrderItemResponse;
import com.tmt.ecommerce.order.dto.response.OrderResponse;
import com.tmt.ecommerce.order.entity.DeliveryConfirmationSource;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderItem;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderItemRepository;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.order.repository.OrderVariantProjection;
import com.tmt.ecommerce.order.repository.ShopFulfilledOrderValueProjection;
import com.tmt.ecommerce.product.api.ProductInternalService;
import com.tmt.ecommerce.product.api.dto.ProductVariantInfoDto;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import com.tmt.ecommerce.voucher.api.VoucherInternalService;
import com.tmt.ecommerce.voucher.api.dto.VoucherCalculationRequest;
import com.tmt.ecommerce.voucher.api.dto.VoucherDiscountResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService, OrderInternalService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartInternalService cartInternalService;
    private final ProductInternalService productInternalService;
    private final ShopInternalService shopInternalService;
    private final IdentityInternalService identityInternalService;
    private final VoucherInternalService voucherInternalService;
    private final ApplicationEventPublisher eventPublisher;

    private static final Map<OrderStatus, List<OrderStatus>> VENDOR_STATE_TRANSITIONS = Map.of(
            OrderStatus.PENDING, List.of(OrderStatus.CONFIRMED),
            OrderStatus.CONFIRMED, List.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED)
    );

    @Override
    @Transactional
    public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
        String paymentGroupId = UUID.randomUUID().toString();
        List<CartItemInternalDto> cartItems = resolveCheckoutItems(userId, request.cartItemIds());

        cartItems.stream().map(CartItemInternalDto::shopId).distinct().sorted()
                .forEach(shopInternalService::requireNotBannedForSale);

        Map<Long, List<CartItemInternalDto>> itemsByShop = cartItems.stream()
                .collect(Collectors.groupingBy(CartItemInternalDto::shopId));

        VoucherDiscountResult voucherDiscount = null;
        if (request.voucherCode() != null && !request.voucherCode().trim().isEmpty()) {
            Map<Long, BigDecimal> shopTotals = calculateShopTotals(itemsByShop);
            VoucherCalculationRequest calcRequest = new VoucherCalculationRequest(request.voucherCode(), shopTotals);
            voucherDiscount = voucherInternalService.calculateDiscount(calcRequest);

            boolean reserved = voucherInternalService.incrementUsage(voucherDiscount.voucherId());
            if (!reserved) {
                throw new IllegalStateException("Mã giảm giá đã hết lượt sử dụng (có người vừa dùng lượt cuối cùng)");
            }
        }

        List<Order> savedOrders = new ArrayList<>();
        List<Long> allCheckoutVariantIds = cartItems.stream().map(CartItemInternalDto::productVariantId).distinct().toList();
        Map<Long, ProductVariantInfoDto> checkoutVariantInfoMap = productInternalService.getVariantInfos(allCheckoutVariantIds);

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

                ProductVariantInfoDto vInfo = checkoutVariantInfoMap.get(cartItem.productVariantId());
                String itemImageUrl = vInfo != null ? vInfo.thumbnailUrl() : null;

                OrderItem orderItem = OrderItem.builder()
                        .productVariantId(cartItem.productVariantId())
                        .productName(cartItem.productName())
                        .imageUrl(itemImageUrl)
                        .unitPrice(cartItem.unitPrice())
                        .quantity(cartItem.quantity())
                        .subTotal(subTotal)
                        .build();

                order.addOrderItem(orderItem);
            }

            BigDecimal shopDiscount = BigDecimal.ZERO;
            if (voucherDiscount != null && voucherDiscount.discountPerShop().containsKey(shopId)) {
                shopDiscount = voucherDiscount.discountPerShop().get(shopId);
                if (shopDiscount.compareTo(BigDecimal.ZERO) > 0) {
                    order.setAppliedVoucherId(voucherDiscount.voucherId());
                    order.setAppliedVoucherCode(voucherDiscount.code());
                }
            }

            order.setOriginalTotalAmount(shopOrderTotal);
            order.setDiscountAmount(shopDiscount);
            order.setTotalAmount(shopOrderTotal.subtract(shopDiscount));

            savedOrders.add(orderRepository.save(order));
        }

        if (!"VNPAY".equalsIgnoreCase(request.paymentMethod())) {
            List<Long> checkedOutItemIds = cartItems.stream().map(CartItemInternalDto::cartItemId).toList();
            cartInternalService.removeCartItems(userId, checkedOutItemIds);
        }

        List<OrderResponse> orderResponses = mapToOrderResponses(savedOrders);
        return new CheckoutResponse(paymentGroupId, orderResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public CartPreviewResponse previewCheckout(Long userId, CartPreviewRequest request) {
        List<CartItemInternalDto> cartItems = resolveCheckoutItems(userId, request.cartItemIds());
        Map<Long, List<CartItemInternalDto>> itemsByShop = cartItems.stream()
                .collect(Collectors.groupingBy(CartItemInternalDto::shopId));

        BigDecimal totalCartAmount = BigDecimal.ZERO;
        for (CartItemInternalDto item : cartItems) {
            totalCartAmount = totalCartAmount.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (request.voucherCode() != null && !request.voucherCode().trim().isEmpty()) {
            Map<Long, BigDecimal> shopTotals = calculateShopTotals(itemsByShop);
            VoucherCalculationRequest calcRequest = new VoucherCalculationRequest(request.voucherCode(), shopTotals);
            VoucherDiscountResult voucherDiscount = voucherInternalService.calculateDiscount(calcRequest);

            for (BigDecimal discount : voucherDiscount.discountPerShop().values()) {
                totalDiscount = totalDiscount.add(discount);
            }
        }

        return CartPreviewResponse.builder()
                .subtotal(totalCartAmount)
                .discount(totalDiscount)
                .total(totalCartAmount.subtract(totalDiscount))
                .build();
    }

    private List<CartItemInternalDto> resolveCheckoutItems(Long userId, List<Long> requestedItemIds) {
        List<CartItemInternalDto> allCartItems = cartInternalService.getCartItems(userId);
        if (requestedItemIds != null && !requestedItemIds.isEmpty()) {
            List<CartItemInternalDto> filtered = allCartItems.stream()
                    .filter(item -> requestedItemIds.contains(item.cartItemId()))
                    .toList();
            if (filtered.isEmpty()) {
                throw new BusinessException("Các sản phẩm đã chọn không tồn tại trong giỏ hàng!");
            }
            return filtered;
        }
        if (allCartItems.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống, không thể thanh toán!");
        }
        return allCartItems;
    }

    private Map<Long, BigDecimal> calculateShopTotals(Map<Long, List<CartItemInternalDto>> itemsByShop) {
        Map<Long, BigDecimal> shopTotals = new HashMap<>();
        for (Map.Entry<Long, List<CartItemInternalDto>> entry : itemsByShop.entrySet()) {
            BigDecimal subTotal = BigDecimal.ZERO;
            for (CartItemInternalDto item : entry.getValue()) {
                subTotal = subTotal.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
            }
            shopTotals.put(entry.getKey(), subTotal);
        }
        return shopTotals;
    }

    private List<OrderResponse> mapToOrderResponses(List<Order> orders) {
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> variantIds = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .map(OrderItem::getProductVariantId)
                .distinct()
                .toList();

        Map<Long, ProductVariantInfoDto> variantInfoMap = productInternalService.getVariantInfos(variantIds);

        return orders.stream()
                .map(order -> mapToOrderResponseWithMap(order, variantInfoMap))
                .collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponseWithMap(Order order, Map<Long, ProductVariantInfoDto> variantInfoMap) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> {
                    ProductVariantInfoDto vInfo = (variantInfoMap != null && item.getProductVariantId() != null)
                            ? variantInfoMap.get(item.getProductVariantId())
                            : null;
                    String img = item.getImageUrl();
                    if ((img == null || img.isBlank()) && vInfo != null) {
                        img = vInfo.thumbnailUrl();
                    }
                    Long productId = vInfo != null ? vInfo.productId() : null;
                    return OrderItemResponse.builder()
                            .id(item.getId())
                            .productVariantId(item.getProductVariantId())
                            .productId(productId)
                            .productName(item.getProductName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subTotal(item.getSubTotal())
                            .imageUrl(img)
                            .build();
                })
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
                .deliveredAt(order.getDeliveredAt())
                .deliveryConfirmationSource(order.getDeliveryConfirmationSource() == null
                        ? null
                        : order.getDeliveryConfirmationSource().name())
                .paymentGroupId(order.getPaymentGroupId())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;

        if (status != null && !status.trim().isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orderPage = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            }
        } else {
            orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        List<OrderResponse> responseList = mapToOrderResponses(orderPage.getContent());
        return new PageImpl<>(responseList, pageable, orderPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền xem đơn hàng này");
        }

        return mapToOrderResponses(List.of(order)).get(0);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATE_TRANSITION);
        }

        log.info("Buyer userId={} canceled orderId={}", userId, orderId);
        return applyOrderTransition(order, OrderStatus.CANCELLED);
    }

    @Transactional
    public void cancelOrderSystem(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        log.info("System auto-canceled expired orderId={}", orderId);
        applyOrderTransition(order, OrderStatus.CANCELLED);
    }

    @Override
    @Transactional
    public OrderResponse confirmDelivery(Long userId, Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATE_TRANSITION);
        }
        return applyOrderTransition(order, OrderStatus.DELIVERED, DeliveryConfirmationSource.BUYER, userId);
    }

    @Override
    public OrderPaymentDto getPaymentDataForGroup(String paymentGroupId) {
        List<Order> orders = orderRepository.findByPaymentGroupId(paymentGroupId);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy nhóm đơn hàng");
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
            throw new ResourceNotFoundException("Không tìm thấy nhóm đơn hàng");
        }

        for (Order order : orders) {
            if (!order.getUserId().equals(userId)) {
                throw new AccessDeniedException("Bạn không có quyền thanh toán nhóm đơn hàng này");
            }
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new BusinessException("Chỉ đơn hàng ở trạng thái PENDING mới có thể thanh toán");
            }
        }
    }

    @Override
    @Transactional
    public void confirmPendingOrdersByGroup(String paymentGroupId) {
        List<Order> orders = orderRepository.findByPaymentGroupIdForUpdate(paymentGroupId);
        Map<Long, OrderStatus> skipped = new LinkedHashMap<>();
        List<Long> confirmedVariantIds = new ArrayList<>();
        Long orderUserId = null;

        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.PENDING) {
                skipped.put(order.getId(), order.getStatus());
                continue;
            }
            order.setStatus(OrderStatus.CONFIRMED);
            if (orderUserId == null) {
                orderUserId = order.getUserId();
            }
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    confirmedVariantIds.add(item.getProductVariantId());
                }
            }
            eventPublisher.publishEvent(new OrderConfirmedEvent(
                    order.getId(), order.getUserId(), order.getShopId(), order.getPaymentMethod()));
        }

        if (orderUserId != null && !confirmedVariantIds.isEmpty()) {
            cartInternalService.removeCartItemsByVariantIds(orderUserId, confirmedVariantIds);
        }

        boolean missingGroup = orders.isEmpty();
        if (missingGroup || !skipped.isEmpty()) {
            log.warn("event=payment_order_confirmation_skipped paymentGroupId={} skippedOrders={} missingGroup={}",
                    paymentGroupId, skipped, missingGroup);
        }
    }

    @Override
    @Transactional
    public void cancelPendingOrdersByGroup(String paymentGroupId, String reason) {
        List<Order> orders = orderRepository.findByPaymentGroupIdForUpdate(paymentGroupId);
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CANCELLED);

                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        try {
                            productInternalService.restoreStock(item.getProductVariantId(), item.getQuantity());
                        } catch (Exception e) {
                            log.error("Failed to restore stock for variant {} in order {}: {}",
                                    item.getProductVariantId(), order.getId(), e.getMessage());
                        }
                    }
                }

                if (order.getAppliedVoucherId() != null) {
                    try {
                        voucherInternalService.decrementUsage(order.getAppliedVoucherId());
                    } catch (Exception e) {
                        log.error("Failed to decrement voucher usage {} for order {}: {}",
                                order.getAppliedVoucherId(), order.getId(), e.getMessage());
                    }
                }

                List<OrderItemCancelDto> cancelItems = order.getItems() != null
                        ? order.getItems().stream()
                                .map(item -> new OrderItemCancelDto(item.getProductVariantId(), item.getQuantity()))
                                .toList()
                        : List.of();

                eventPublisher.publishEvent(new OrderCancelledEvent(
                        order.getId(), order.getUserId(), order.getShopId(), cancelItems));
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAdminOrders(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;

        if (status != null && !status.trim().isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orderPage = orderRepository.findByStatusOrderByCreatedAtDesc(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                orderPage = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        } else {
            orderPage = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<OrderResponse> responseList = mapToOrderResponses(orderPage.getContent());
        return new PageImpl<>(responseList, pageable, orderPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getAdminOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        return mapToOrderResponses(List.of(order)).get(0);
    }

    @Override
    @Transactional(readOnly = true)
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

        List<OrderResponse> responseList = mapToOrderResponses(orderPage.getContent());
        return new PageImpl<>(responseList, pageable, orderPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getVendorOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (!shopInternalService.isShopOwner(order.getShopId(), userId)) {
            throw new AccessDeniedException("Bạn không có quyền xem đơn hàng này.");
        }

        return mapToOrderResponses(List.of(order)).get(0);
    }

    @Override
    @Transactional
    public OrderResponse updateVendorOrderStatus(Long userId, Long orderId, VendorOrderStatusUpdateRequest request) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!shopInternalService.isShopOwner(order.getShopId(), userId)) {
            throw new AccessDeniedException("Bạn không có quyền cập nhật đơn hàng này.");
        }

        if ("VNPAY".equalsIgnoreCase(order.getPaymentMethod()) && order.getStatus() == OrderStatus.PENDING && request.status() == OrderStatus.CONFIRMED) {
            throw new BusinessException("Đơn hàng thanh toán bằng VNPAY đang chờ khách hàng thanh toán, không thể xác nhận thủ công.");
        }

        validateVendorStateTransition(order.getStatus(), request.status());

        log.info("Vendor userId={} cập nhật đơn hàng #{} từ {} sang {}", userId, orderId, order.getStatus(), request.status());
        return applyOrderTransition(order, request.status());
    }

    OrderResponse applyOrderTransition(Order order, OrderStatus target) {
        return applyOrderTransition(order, target, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundableOrderSnapshot getRefundableOrderSnapshot(long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return new RefundableOrderSnapshot(order.getId(), order.getUserId(),
                order.getShopId(), order.getPaymentGroupId(), order.getPaymentMethod(),
                order.getStatus().name(), order.getTotalAmount());
    }

    OrderResponse applyOrderTransition(Order order, OrderStatus target,
            DeliveryConfirmationSource deliverySource, Long deliveryActorUserId) {
        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == OrderStatus.SHIPPED && target == OrderStatus.DELIVERED
                && (deliverySource == null || deliveryActorUserId == null)) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        order.setStatus(target);
        if (oldStatus == OrderStatus.SHIPPED && target == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            order.setDeliveryConfirmedByUserId(deliveryActorUserId);
            order.setDeliveryConfirmationSource(deliverySource);
        }
        if (target == OrderStatus.CANCELLED) {
            if (order.getItems() == null || order.getItems().isEmpty()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            for (OrderItem item : order.getItems()) {
                productInternalService.restoreStock(item.getProductVariantId(), item.getQuantity());
            }
        }
        Order updatedOrder = orderRepository.save(order);

        OrderStatus newStatus = updatedOrder.getStatus();
        if (oldStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) {
            eventPublisher.publishEvent(new OrderConfirmedEvent(
                    updatedOrder.getId(), updatedOrder.getUserId(), updatedOrder.getShopId(),
                    updatedOrder.getPaymentMethod()));
        } else if (oldStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.SHIPPED) {
            eventPublisher.publishEvent(new OrderShippedEvent(
                    updatedOrder.getId(), updatedOrder.getUserId()));
        } else if (oldStatus == OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED) {
            eventPublisher.publishEvent(new OrderDeliveredEvent(
                    updatedOrder.getId(), updatedOrder.getUserId()));
        } else if (newStatus == OrderStatus.CANCELLED) {
            List<OrderItemCancelDto> cancelItems = updatedOrder.getItems() != null
                    ? updatedOrder.getItems().stream()
                            .map(item -> new OrderItemCancelDto(item.getProductVariantId(), item.getQuantity()))
                            .toList()
                    : List.of();

            eventPublisher.publishEvent(new OrderCancelledEvent(
                    updatedOrder.getId(), updatedOrder.getUserId(), updatedOrder.getShopId(), cancelItems));
        }

        return mapToOrderResponses(List.of(updatedOrder)).get(0);
    }

    private void validateVendorStateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        List<OrderStatus> allowedTransitions = VENDOR_STATE_TRANSITIONS.get(currentStatus);
        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new AppException(
                    ErrorCode.INVALID_ORDER_STATE_TRANSITION,
                    String.format("Không thể chuyển trạng thái đơn hàng từ %s sang %s", currentStatus, newStatus));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderDeliveredAndBelongsToUser(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        return order.getUserId().equals(userId) && order.getStatus() == OrderStatus.DELIVERED;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getProductVariantIdsByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return new ArrayList<>();
        }
        return order.getItems().stream()
                .map(OrderItem::getProductVariantId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveredOrderData> getDeliveredOrdersWithVariantsByUserId(Long userId) {
        List<OrderVariantProjection> projections = orderRepository.findDeliveredOrderVariants(userId, OrderStatus.DELIVERED);

        Map<Long, List<Long>> grouped = projections.stream()
                .collect(Collectors.groupingBy(
                        OrderVariantProjection::getOrderId,
                        Collectors.mapping(OrderVariantProjection::getProductVariantId, Collectors.toList())));

        return grouped.entrySet().stream()
                .map(entry -> new DeliveredOrderData(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardOrderStatsDto getDashboardOrderStats(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now().plusYears(1);

        long totalOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.PENDING, start, end)
                + orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.CONFIRMED, start, end)
                + orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.SHIPPED, start, end)
                + orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, start, end)
                + orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.CANCELLED, start, end);

        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            ordersByStatus.put(s.name(), orderRepository.countByStatusAndCreatedAtBetween(s, start, end));
        }

        BigDecimal deliveredRevenue = orderRepository.sumDeliveredRevenueByCreatedAtBetween(start, end);
        if (deliveredRevenue == null) {
            deliveredRevenue = BigDecimal.ZERO;
        }

        long deliveredCount = ordersByStatus.getOrDefault(OrderStatus.DELIVERED.name(), 0L);
        BigDecimal aov = deliveredCount > 0
                ? deliveredRevenue.divide(BigDecimal.valueOf(deliveredCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<DashboardOrderStatsDto.DailyChartPointDto> dailyChart = orderRepository
                .getDailyOrderChartData(start, end).stream()
                .map(row -> new DashboardOrderStatsDto.DailyChartPointDto(
                        row[0].toString(),
                        ((Number) row[1]).longValue(),
                        BigDecimal.ZERO))
                .collect(Collectors.toList());

        List<DashboardOrderStatsDto.RecentOrderDto> recent = orderRepository
                .findTop10ByOrderByCreatedAtDesc().stream()
                .map(o -> new DashboardOrderStatsDto.RecentOrderDto(
                        o.getId(), o.getUserId(), o.getShopId(),
                        o.getStatus().name(), o.getTotalAmount(),
                        o.getPaymentMethod(), o.getCreatedAt()))
                .collect(Collectors.toList());

        return new DashboardOrderStatsDto(totalOrders, ordersByStatus, deliveredRevenue, aov, dailyChart, recent);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopOrderAnalyticsDto getShopOrderAnalytics(Long shopId, LocalDateTime startDate, LocalDateTime endDate) {
        if (shopId == null) {
            throw new AppException(ErrorCode.SHOP_NOT_FOUND);
        }

        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), 0L);
        }
        orderRepository.getShopOrderStatusCounts(shopId, startDate, endDate)
                .forEach(row -> ordersByStatus.put(row.getStatus(), row.getOrderCount()));

        long totalOrders = ordersByStatus.values().stream().mapToLong(Long::longValue).sum();
        ShopFulfilledOrderValueProjection fulfilled = orderRepository.getShopFulfilledOrderValues(shopId, startDate, endDate);
        long deliveredCount = fulfilled != null ? fulfilled.getDeliveredOrderCount() : 0L;
        BigDecimal gross = fulfilled != null && fulfilled.getFulfilledGrossOrderValue() != null
                ? fulfilled.getFulfilledGrossOrderValue()
                : BigDecimal.ZERO;
        BigDecimal discount = fulfilled != null && fulfilled.getVoucherDiscountAmount() != null
                ? fulfilled.getVoucherDiscountAmount()
                : BigDecimal.ZERO;
        BigDecimal value = fulfilled != null && fulfilled.getFulfilledOrderValue() != null
                ? fulfilled.getFulfilledOrderValue()
                : BigDecimal.ZERO;
        BigDecimal aov = deliveredCount == 0 ? BigDecimal.ZERO
                : value.divide(BigDecimal.valueOf(deliveredCount), 2, RoundingMode.HALF_UP);

        List<ShopOrderAnalyticsDto.DailyChartPointDto> dailyChart = orderRepository
                .getShopDailyOrderAnalytics(shopId, startDate, endDate).stream()
                .map(row -> new ShopOrderAnalyticsDto.DailyChartPointDto(
                        row[0].toString(),
                        ((Number) row[1]).longValue(),
                        row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString())))
                .toList();

        return new ShopOrderAnalyticsDto(totalOrders, ordersByStatus, deliveredCount, gross, discount, value, aov, dailyChart);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getTopSellingVariantIds(int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));
        return orderItemRepository.findTopSellingVariantIds(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getRecentlyPurchasedVariantIds(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));
        return orderItemRepository.findRecentlyPurchasedVariantIds(userId, pageable);
    }
}

