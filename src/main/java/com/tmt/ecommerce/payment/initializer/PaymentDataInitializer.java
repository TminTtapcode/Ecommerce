package com.tmt.ecommerce.payment.initializer;

import com.tmt.ecommerce.common.constant.SeedDataIds;
import com.tmt.ecommerce.payment.entity.Payment;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.enums.PaymentStatus;
import com.tmt.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@Order(6)
@RequiredArgsConstructor
public class PaymentDataInitializer implements CommandLineRunner {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (paymentRepository.count() > 0) {
            log.info("Payments already exist. Skipping PaymentDataInitializer.");
            return;
        }

        log.info("=== STARTING PAYMENT SEED DATA ===");

        createPayment(SeedDataIds.PAYMENT_GROUP_1, new BigDecimal("29990000"), PaymentMethod.VNPAY, PaymentStatus.SUCCESS, "PAY_" + UUID.randomUUID().toString().substring(0, 8));
        createPayment(SeedDataIds.PAYMENT_GROUP_2, new BigDecimal("890000"), PaymentMethod.COD, PaymentStatus.PENDING, "PAY_" + UUID.randomUUID().toString().substring(0, 8));
        createPayment(SeedDataIds.PAYMENT_GROUP_3, new BigDecimal("1850000"), PaymentMethod.VNPAY, PaymentStatus.SUCCESS, "PAY_" + UUID.randomUUID().toString().substring(0, 8));

        log.info("=== FINISHED PAYMENT SEED DATA ===");
        log.info("=== HOÀN TẤT NẠP DỮ LIỆU MẪU TOÀN HỆ THỐNG PHỦ 13 BANG ===");
    }

    private void createPayment(String paymentGroupId, BigDecimal amount, PaymentMethod method, PaymentStatus status, String txnId) {
        Payment payment = Payment.builder()
                .paymentGroupId(paymentGroupId)
                .amount(amount)
                .method(method)
                .status(status)
                .transactionId(txnId)
                .gatewayTransactionId("GW_" + txnId)
                .rawData("{\"code\":\"00\",\"msg\":\"Success\"}")
                .build();
        paymentRepository.save(payment);
    }
}
