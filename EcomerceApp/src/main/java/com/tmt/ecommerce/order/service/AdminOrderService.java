package com.tmt.ecommerce.order.service;

import com.tmt.ecommerce.common.exception.*;
import com.tmt.ecommerce.common.security.CurrentUserPrincipal;
import com.tmt.ecommerce.order.dto.request.AdminOrderStatusRequest;
import com.tmt.ecommerce.order.dto.response.*;
import com.tmt.ecommerce.order.entity.*;
import com.tmt.ecommerce.order.repository.*;
import com.tmt.ecommerce.payment.api.PaymentEligibilityQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import java.time.Instant;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderService {
    private final OrderRepository orders;
    private final OrderAdminStatusHistoryRepository history;
    private final OrderServiceImpl orderService;
    private final PaymentEligibilityQuery paymentEligibility;

    private Order load(Long id, boolean lock) {
        return (lock ? orders.findByIdForUpdate(id) : orders.findById(id))
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }
    private List<OrderStatus> transitions(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> List.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED);
            case SHIPPED -> List.of(OrderStatus.DELIVERED);
            default -> List.of();
        };
    }
    private boolean paymentAllows(Order order, OrderStatus target) {
        if ("COD".equalsIgnoreCase(order.getPaymentMethod())) return true;
        return "VNPAY".equalsIgnoreCase(order.getPaymentMethod()) && target != OrderStatus.CANCELLED
                && paymentEligibility.hasSuccessfulVnpayPayment(order.getPaymentGroupId());
    }
    @Transactional(readOnly = true)
    public List<OrderStatus> allowedTransitions(Long id) {
        Order order = load(id, false);
        return transitions(order.getStatus()).stream().filter(target -> paymentAllows(order, target)).toList();
    }
    @Transactional(readOnly = true)
    public Page<OrderAdminHistoryResponse> history(Long id, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new AppException(ErrorCode.INVALID_INPUT);
        load(id, false);
        return history.findByOrderIdOrderByIdDesc(id, PageRequest.of(page, size)).map(OrderAdminHistoryResponse::from);
    }
    @Transactional
    public OrderResponse update(Long id, AdminOrderStatusRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserPrincipal principal)
                || principal.getId() == null) throw new AccessDeniedException("Missing admin principal");
        if (request == null || request.expectedStatus() == null || request.status() == null
                || request.reason() == null || request.reason().isBlank() || request.reason().length() > 500)
            throw new AppException(ErrorCode.INVALID_INPUT);
        Order order = load(id, true);
        if (order.getStatus() != request.expectedStatus()) throw new AppException(ErrorCode.ORDER_STATE_CONFLICT);
        if (!transitions(order.getStatus()).contains(request.status()))
            throw new AppException(ErrorCode.INVALID_ORDER_STATE_TRANSITION);

        if (!paymentAllows(order, request.status())) throw new AppException(ErrorCode.ORDER_PAYMENT_CONDITION_NOT_MET);
        OrderStatus oldStatus = order.getStatus();
        OrderResponse response = orderService.applyOrderTransition(order, request.status(),
                request.status() == OrderStatus.DELIVERED
                        ? DeliveryConfirmationSource.ADMIN : null,
                request.status() == OrderStatus.DELIVERED ? principal.getId() : null);
        var entry = history.saveAndFlush(OrderAdminStatusHistory.builder().orderId(id).actorUserId(principal.getId())
                .oldStatus(oldStatus).newStatus(request.status()).reason(request.reason()).createdAt(Instant.now()).build());
        Long historyId = entry.getId(), actorId = principal.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                log.info("event=admin_order_status_changed historyId={} orderId={} actorId={} oldStatus={} newStatus={}",
                        historyId, id, actorId, oldStatus, request.status());
            }
        });
        return response;
    }
}
