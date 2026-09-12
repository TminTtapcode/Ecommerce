package com.tmt.ecommerce.payment.service;

import com.tmt.ecommerce.payment.api.PaymentEligibilityQuery;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.enums.PaymentStatus;
import com.tmt.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentEligibilityQueryImpl implements PaymentEligibilityQuery {
    private final PaymentRepository payments;

    @Override
    @Transactional(readOnly = true)
    public boolean hasSuccessfulVnpayPayment(String group) {
        return group != null && !group.isBlank()
                && payments.existsByPaymentGroupIdAndMethodAndStatus(group, PaymentMethod.VNPAY, PaymentStatus.SUCCESS);
    }
}
