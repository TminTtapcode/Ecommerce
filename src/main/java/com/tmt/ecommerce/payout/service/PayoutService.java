package com.tmt.ecommerce.payout.service;

import com.tmt.ecommerce.payout.dto.request.ApprovePayoutRequest;
import com.tmt.ecommerce.payout.dto.request.CreatePayoutRequest;
import com.tmt.ecommerce.payout.dto.request.RejectPayoutRequest;
import com.tmt.ecommerce.payout.dto.response.PayoutBalanceResponse;
import com.tmt.ecommerce.payout.dto.response.PayoutResponse;
import com.tmt.ecommerce.payout.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayoutService {

    PayoutBalanceResponse getShopBalance(Long userId);

    PayoutResponse createPayoutRequest(Long userId, CreatePayoutRequest request);

    Page<PayoutResponse> getShopPayouts(Long userId, Pageable pageable);

    Page<PayoutResponse> getAdminPayouts(PayoutStatus status, Pageable pageable);

    PayoutResponse approvePayout(Long payoutId, Long adminUserId, ApprovePayoutRequest request);

    PayoutResponse rejectPayout(Long payoutId, Long adminUserId, RejectPayoutRequest request);
}
