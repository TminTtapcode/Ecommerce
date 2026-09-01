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

@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final PaymentStrategyFactory paymentFactory;
    private final PaymentRepository paymentRepository;
    private final OrderInternalService orderInternalService;

    @Transactional
    public String generatePaymentUrl(PaymentOrderData orderData, String clientIp) {
        PaymentStrategy strategy = paymentFactory.getStrategy(orderData.method());
        String txnRef = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Payment payment = Payment.builder()
                .paymentGroupId(orderData.paymentGroupId())
                .amount(orderData.amount())
                .method(orderData.method())
                .status(PaymentStatus.PENDING)
                .transactionId(txnRef)
                .build();
        paymentRepository.save(payment);

        return strategy.createPaymentUrl(orderData, clientIp, txnRef);
    }

    // THÊM MỚI HÀM NÀY ĐỂ FIX LỖI
    @Transactional
    public void processVnPayIpn(Map<String, String> params) {
        // 1. Lấy đúng Strategy của VNPAY ra để check chữ ký
        PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.VNPAY);

        if (!strategy.verifyIpnSignature(params)) {
            throw new IllegalArgumentException("Chữ ký VNPAY không hợp lệ! Phát hiện nghi vấn giả mạo.");
        }

        // 2. Lấy các trường dữ liệu quan trọng từ Webhook
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String gatewayTxnId = params.get("vnp_TransactionNo");

        // 3. Tìm giao dịch trong DB với Khóa bi quan (Pessimistic Lock) để chống Race Condition khi có 2 IPN đến cùng lúc
        Payment payment = paymentRepository.findByTransactionIdForUpdate(txnRef)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch: " + txnRef));

        // 4. Chống lặp (Idempotency) - Nếu giao dịch đã được xử lý (SUCCESS hoặc FAILED), ngắt ngay lập tức
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        // 5. Kiểm tra số tiền
        String vnpAmountStr = params.get("vnp_Amount");
        if (vnpAmountStr != null) {
            long vnpAmount = Long.parseLong(vnpAmountStr);
            long expectedAmount = payment.getAmount().longValue() * 100L;
            if (vnpAmount != expectedAmount) {
                throw new IllegalArgumentException("Số tiền thanh toán không khớp!");
            }
        }

        // 6. Cập nhật trạng thái
        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            // Thanh toán thành công -> Đơn hàng chuyển sang CONFIRMED (Chờ Shop xử lý hàng)
            orderInternalService.updateOrderStatusByGroup(payment.getPaymentGroupId(), "CONFIRMED");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            // Thanh toán thất bại -> Giữ nguyên Order ở trạng thái PENDING để người dùng có thể bấm Thanh toán lại (Retry)
        }

        // 7. Lưu mã chuẩn chi của VNPAY và Raw Data để đối soát sau này
        payment.setGatewayTransactionId(gatewayTxnId);
        payment.setRawData(params.toString());

        paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public PaymentGroupStatusResponse getPaymentGroupStatus(String paymentGroupId) {
        var payments = paymentRepository.findByPaymentGroupIdOrderByCreatedAtDesc(paymentGroupId);
        if (payments.isEmpty()) {
            return new PaymentGroupStatusResponse(paymentGroupId, "NOT_FOUND");
        }

        // Nếu có ít nhất 1 lần thanh toán thành công -> Group status = SUCCESS
        boolean hasSuccess = payments.stream().anyMatch(p -> p.getStatus() == PaymentStatus.SUCCESS);
        if (hasSuccess) {
            return new PaymentGroupStatusResponse(paymentGroupId, "SUCCESS");
        }

        // Nếu lượt thử gần nhất vẫn đang PENDING
        if (payments.get(0).getStatus() == PaymentStatus.PENDING) {
            return new PaymentGroupStatusResponse(paymentGroupId, "PENDING");
        }

        // Ngược lại (lượt thử gần nhất bị FAILED)
        return new PaymentGroupStatusResponse(paymentGroupId, "FAILED");
    }
}