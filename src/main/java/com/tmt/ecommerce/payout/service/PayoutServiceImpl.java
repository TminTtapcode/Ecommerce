package com.tmt.ecommerce.payout.service;

import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.order.api.OrderInternalService;
import com.tmt.ecommerce.order.api.dto.ShopOrderAnalyticsDto;
import com.tmt.ecommerce.payout.dto.request.ApprovePayoutRequest;
import com.tmt.ecommerce.payout.dto.request.CreatePayoutRequest;
import com.tmt.ecommerce.payout.dto.request.RejectPayoutRequest;
import com.tmt.ecommerce.payout.dto.response.PayoutBalanceResponse;
import com.tmt.ecommerce.payout.dto.response.PayoutResponse;
import com.tmt.ecommerce.payout.entity.PayoutRequest;
import com.tmt.ecommerce.payout.enums.PayoutStatus;
import com.tmt.ecommerce.payout.repository.PayoutRequestRepository;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutServiceImpl implements PayoutService {

    public static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05"); // 5% Chiết khấu sàn
    public static final BigDecimal MIN_PAYOUT_AMOUNT = new BigDecimal("50000"); // 50.000đ

    private final PayoutRequestRepository payoutRequestRepository;
    private final ShopInternalService shopInternalService;
    private final OrderInternalService orderInternalService;

    @Override
    @Transactional(readOnly = true)
    public PayoutBalanceResponse getShopBalance(Long userId) {
        Long shopId = getShopIdForUser(userId);
        return calculateShopBalance(shopId);
    }

    @Override
    @Transactional
    public PayoutResponse createPayoutRequest(Long userId, CreatePayoutRequest request) {
        Long shopId = getShopIdForUser(userId);
        shopInternalService.requireNotBannedForSale(shopId);

        if (request.getAmount() == null || request.getAmount().compareTo(MIN_PAYOUT_AMOUNT) < 0) {
            throw new AppException(ErrorCode.PAYOUT_AMOUNT_INVALID, "Số tiền rút tối thiểu là 50.000 VNĐ");
        }

        PayoutBalanceResponse balance = calculateShopBalance(shopId);
        if (request.getAmount().compareTo(balance.getAvailableBalance()) > 0) {
            throw new AppException(ErrorCode.PAYOUT_BALANCE_INSUFFICIENT,
                    String.format("Số dư khả dụng (%s đ) không đủ để rút %s đ",
                            balance.getAvailableBalance().toPlainString(),
                            request.getAmount().toPlainString()));
        }

        PayoutRequest payout = PayoutRequest.builder()
                .shopId(shopId)
                .amount(request.getAmount())
                .bankName(request.getBankName().trim())
                .bankAccountNumber(request.getBankAccountNumber().trim())
                .bankAccountHolder(request.getBankAccountHolder().trim().toUpperCase())
                .status(PayoutStatus.PENDING)
                .build();

        PayoutRequest saved = payoutRequestRepository.save(payout);
        log.info("Shop {} created payout request #{} for amount {}", shopId, saved.getId(), saved.getAmount());

        return mapToResponse(saved, balance.getShopName());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getShopPayouts(Long userId, Pageable pageable) {
        Long shopId = getShopIdForUser(userId);
        String shopName = shopInternalService.getShopNameById(shopId).orElse("Gian hàng");
        return payoutRequestRepository.findByShopIdOrderByCreatedAtDesc(shopId, pageable)
                .map(p -> mapToResponse(p, shopName));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getAdminPayouts(PayoutStatus status, Pageable pageable) {
        Page<PayoutRequest> page = (status == null)
                ? payoutRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
                : payoutRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

        return page.map(p -> {
            String shopName = shopInternalService.getShopNameById(p.getShopId()).orElse("Gian hàng #" + p.getShopId());
            return mapToResponse(p, shopName);
        });
    }

    @Override
    @Transactional
    public PayoutResponse approvePayout(Long payoutId, Long adminUserId, ApprovePayoutRequest request) {
        PayoutRequest payout = payoutRequestRepository.findById(payoutId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));

        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw new AppException(ErrorCode.PAYOUT_ALREADY_PROCESSED, "Yêu cầu rút tiền này đã được xử lý trước đó");
        }

        payout.setStatus(PayoutStatus.APPROVED);
        payout.setProcessedBy(adminUserId);
        payout.setProcessedAt(LocalDateTime.now());
        payout.setAdminNote(request != null && request.getAdminNote() != null && !request.getAdminNote().isBlank()
                ? request.getAdminNote().trim()
                : "Đã phê duyệt và chuyển tiền thành công");

        PayoutRequest updated = payoutRequestRepository.save(payout);
        log.info("Admin {} approved payout request #{} for shop {}", adminUserId, updated.getId(), updated.getShopId());

        String shopName = shopInternalService.getShopNameById(updated.getShopId()).orElse("Gian hàng");
        return mapToResponse(updated, shopName);
    }

    @Override
    @Transactional
    public PayoutResponse rejectPayout(Long payoutId, Long adminUserId, RejectPayoutRequest request) {
        PayoutRequest payout = payoutRequestRepository.findById(payoutId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));

        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw new AppException(ErrorCode.PAYOUT_ALREADY_PROCESSED, "Yêu cầu rút tiền này đã được xử lý trước đó");
        }

        payout.setStatus(PayoutStatus.REJECTED);
        payout.setProcessedBy(adminUserId);
        payout.setProcessedAt(LocalDateTime.now());
        payout.setAdminNote(request.getReason() != null ? request.getReason().trim() : "Bị từ chối bởi quản trị viên");

        PayoutRequest updated = payoutRequestRepository.save(payout);
        log.info("Admin {} rejected payout request #{} for shop {}", adminUserId, updated.getId(), updated.getShopId());

        String shopName = shopInternalService.getShopNameById(updated.getShopId()).orElse("Gian hàng");
        return mapToResponse(updated, shopName);
    }

    private Long getShopIdForUser(Long userId) {
        return shopInternalService.findShopIdByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND, "Không tìm thấy gian hàng của người dùng"));
    }

    private PayoutBalanceResponse calculateShopBalance(Long shopId) {
        String shopName = shopInternalService.getShopNameById(shopId).orElse("Gian hàng");

        // Doanh thu từ tất cả các đơn hàng hoàn tất (DELIVERED)
        ShopOrderAnalyticsDto analytics = orderInternalService.getShopOrderAnalytics(shopId, null, null);
        BigDecimal grossRevenue = analytics.fulfilledOrderValue() != null ? analytics.fulfilledOrderValue() : BigDecimal.ZERO;
        long deliveredCount = analytics.deliveredOrderCount();

        // Hoa hồng sàn (5%)
        BigDecimal commission = grossRevenue.multiply(COMMISSION_RATE).setScale(0, RoundingMode.HALF_UP);
        BigDecimal netRevenue = grossRevenue.subtract(commission);

        // Tổng tiền đã rút (APPROVED) và đang chờ duyệt (PENDING)
        BigDecimal totalWithdrawn = payoutRequestRepository.sumAmountByShopIdAndStatus(shopId, PayoutStatus.APPROVED);
        BigDecimal pendingWithdrawal = payoutRequestRepository.sumAmountByShopIdAndStatus(shopId, PayoutStatus.PENDING);

        if (totalWithdrawn == null) totalWithdrawn = BigDecimal.ZERO;
        if (pendingWithdrawal == null) pendingWithdrawal = BigDecimal.ZERO;

        BigDecimal availableBalance = netRevenue.subtract(totalWithdrawn).subtract(pendingWithdrawal);
        if (availableBalance.compareTo(BigDecimal.ZERO) < 0) {
            availableBalance = BigDecimal.ZERO;
        }

        return PayoutBalanceResponse.builder()
                .shopId(shopId)
                .shopName(shopName)
                .deliveredOrderCount(deliveredCount)
                .grossDeliveredRevenue(grossRevenue)
                .commissionRate(COMMISSION_RATE)
                .totalCommission(commission)
                .netRevenue(netRevenue)
                .totalWithdrawn(totalWithdrawn)
                .pendingWithdrawal(pendingWithdrawal)
                .availableBalance(availableBalance)
                .build();
    }

    private PayoutResponse mapToResponse(PayoutRequest p, String shopName) {
        return PayoutResponse.builder()
                .id(p.getId())
                .shopId(p.getShopId())
                .shopName(shopName)
                .amount(p.getAmount())
                .bankName(p.getBankName())
                .bankAccountNumber(p.getBankAccountNumber())
                .bankAccountHolder(p.getBankAccountHolder())
                .status(p.getStatus())
                .adminNote(p.getAdminNote())
                .processedBy(p.getProcessedBy())
                .processedAt(p.getProcessedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
