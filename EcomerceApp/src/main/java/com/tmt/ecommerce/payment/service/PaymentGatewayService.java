package com.tmt.ecommerce.payment.service;

import com.tmt.ecommerce.payment.dto.request.PaymentOrderData;
import com.tmt.ecommerce.payment.dto.response.PaymentGroupStatusResponse;
import com.tmt.ecommerce.payment.entity.Payment;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.enums.PaymentStatus;
import com.tmt.ecommerce.order.api.OrderInternalService;
import com.tmt.ecommerce.payment.factory.PaymentStrategyFactory;
import com.tmt.ecommerce.payment.repository.PaymentRepository;
import com.tmt.ecommerce.payment.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PaymentGatewayService {
    private static final DateTimeFormatter VNPAY_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VNPAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final PaymentStrategyFactory paymentFactory;
    private final PaymentRepository paymentRepository;
    private final OrderInternalService orderInternalService;

    @Transactional
    public String generatePaymentUrl(PaymentOrderData orderData, String clientIp) {
        PaymentStrategy strategy = paymentFactory.getStrategy(orderData.method());
        String txnRef = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String vnpayTransactionDate = orderData.method() == PaymentMethod.VNPAY
                ? LocalDateTime.now(VNPAY_ZONE).format(VNPAY_DATE) : null;

        Payment payment = Payment.builder()
                .paymentGroupId(orderData.paymentGroupId())
                .amount(orderData.amount())
                .method(orderData.method())
                .status(PaymentStatus.PENDING)
                .transactionId(txnRef)
                .vnpayTransactionDate(vnpayTransactionDate)
                .build();
        paymentRepository.save(payment);

        return strategy.createPaymentUrl(new PaymentOrderData(orderData.paymentGroupId(), orderData.amount(),
                orderData.description(), orderData.method(), vnpayTransactionDate), clientIp, txnRef);
    }

    @Transactional
    public void processVnPayIpn(Map<String, String> params) {

        PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.VNPAY);

        if (!strategy.verifyIpnSignature(params)) {
            throw new IllegalArgumentException("Chữ ký VNPAY không hợp lệ! Phát hiện nghi vấn giả mạo.");
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String gatewayTxnId = params.get("vnp_TransactionNo");
        String bankCode = params.get("vnp_BankCode");

        Payment payment = paymentRepository.findByTransactionIdForUpdate(txnRef)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch: " + txnRef));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        String vnpAmountStr = params.get("vnp_Amount");
        if (vnpAmountStr == null || !vnpAmountStr.matches("[0-9]+")) {
            throw new IllegalArgumentException("Missing or invalid payment amount");
        }
        try {
            long vnpAmount = Long.parseLong(vnpAmountStr);
            long expectedAmount = payment.getAmount().movePointRight(2).longValueExact();
            if (vnpAmount != expectedAmount) {
                throw new IllegalArgumentException("Payment amount does not match");
            }
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Payment amount cannot be represented exactly", e);
        }

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);

            orderInternalService.confirmPendingOrdersByGroup(payment.getPaymentGroupId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);

        }

        payment.setGatewayTransactionId(gatewayTxnId);
        payment.setGatewayBankCode(bankCode);
        payment.setRawData(params.toString());

        paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public PaymentGroupStatusResponse getPaymentGroupStatus(String paymentGroupId) {
        var payments = paymentRepository.findByPaymentGroupIdOrderByCreatedAtDesc(paymentGroupId);
        if (payments.isEmpty()) {
            return new PaymentGroupStatusResponse(paymentGroupId, "NOT_FOUND");
        }

        boolean hasSuccess = payments.stream().anyMatch(p -> p.getStatus() == PaymentStatus.SUCCESS);
        if (hasSuccess) {
            return new PaymentGroupStatusResponse(paymentGroupId, "SUCCESS");
        }

        if (payments.get(0).getStatus() == PaymentStatus.PENDING) {
            return new PaymentGroupStatusResponse(paymentGroupId, "PENDING");
        }

        return new PaymentGroupStatusResponse(paymentGroupId, "FAILED");
    }
}
