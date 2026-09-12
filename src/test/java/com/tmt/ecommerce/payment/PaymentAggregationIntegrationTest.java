package com.tmt.ecommerce.payment;

import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import com.tmt.ecommerce.order.service.OrderServiceImpl;
import com.tmt.ecommerce.payment.dto.request.PaymentOrderData;
import com.tmt.ecommerce.payment.entity.Payment;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.enums.PaymentStatus;
import com.tmt.ecommerce.payment.factory.PaymentStrategyFactory;
import com.tmt.ecommerce.payment.repository.PaymentRepository;
import com.tmt.ecommerce.payment.service.PaymentGatewayService;
import com.tmt.ecommerce.payment.strategy.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class PaymentAggregationIntegrationTest {

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentStrategyFactory paymentStrategyFactory;

    @MockitoBean(name = "vnPayStrategy")
    private PaymentStrategy mockVnPayStrategy;

    @BeforeEach
    void setUp() {
        when(paymentStrategyFactory.getStrategy(PaymentMethod.VNPAY)).thenReturn(mockVnPayStrategy);
        when(mockVnPayStrategy.verifyIpnSignature(any())).thenReturn(true);
        when(mockVnPayStrategy.createPaymentUrl(any(), any(), any())).thenReturn("http://vnpay.sandbox.vn/pay");
    }

    @Test
    @DisplayName("Scenario 1: 2-Shop Checkout -> VNPay Success -> Both Orders updated to CONFIRMED")
    void testTwoShopCheckoutSuccess() {
        String paymentGroupId = UUID.randomUUID().toString();
        Long userId = 999L;

        // Tạo 2 đơn hàng thuộc 2 shop khác nhau nhưng chung paymentGroupId
        Order orderShop1 = Order.builder()
                .userId(userId)
                .shopId(101L)
                .paymentGroupId(paymentGroupId)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100000"))
                .shippingAddress("123 Street")
                .paymentMethod("VNPAY")
                .build();

        Order orderShop2 = Order.builder()
                .userId(userId)
                .shopId(102L)
                .paymentGroupId(paymentGroupId)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("200000"))
                .shippingAddress("123 Street")
                .paymentMethod("VNPAY")
                .build();

        orderRepository.saveAll(List.of(orderShop1, orderShop2));

        // 1. Tạo Payment URL cho cả nhóm (Tổng = 300,000)
        PaymentOrderData orderData = new PaymentOrderData(
                paymentGroupId,
                new BigDecimal("300000"),
                "Thanh toan 2 shop",
                PaymentMethod.VNPAY
        );
        String paymentUrl = paymentGatewayService.generatePaymentUrl(orderData, "127.0.0.1");
        assertNotNull(paymentUrl);

        // Lấy Payment record vừa được tạo
        List<Payment> payments = paymentRepository.findByPaymentGroupIdOrderByCreatedAtDesc(paymentGroupId);
        assertEquals(1, payments.size());
        Payment payment = payments.get(0);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(0, new BigDecimal("300000").compareTo(payment.getAmount()));

        // 2. Giả lập VNPay IPN báo thành công (responseCode = 00)
        Map<String, String> ipnParams = new HashMap<>();
        ipnParams.put("vnp_TxnRef", payment.getTransactionId());
        ipnParams.put("vnp_ResponseCode", "00");
        ipnParams.put("vnp_TransactionNo", "VNP12345678");
        ipnParams.put("vnp_Amount", "30000000"); // 300,000 * 100

        paymentGatewayService.processVnPayIpn(ipnParams);

        // 3. Kiểm tra kết quả
        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, updatedPayment.getStatus());
        assertEquals("VNP12345678", updatedPayment.getGatewayTransactionId());

        // Cả 2 Order đều phải được đổi sang CONFIRMED
        List<Order> updatedOrders = orderRepository.findByPaymentGroupId(paymentGroupId);
        assertEquals(2, updatedOrders.size());
        assertTrue(updatedOrders.stream().allMatch(o -> o.getStatus() == OrderStatus.CONFIRMED));

        // Polling Payment Status
        var groupStatus = paymentGatewayService.getPaymentGroupStatus(paymentGroupId);
        assertEquals("SUCCESS", groupStatus.getStatus());
    }

    @Test
    @DisplayName("Scenario 2: Failed Attempt -> Orders stay PENDING -> Retry Attempt -> Success -> Orders CONFIRMED")
    void testFailedThenRetrySuccess() {
        String paymentGroupId = UUID.randomUUID().toString();
        Long userId = 888L;

        Order order = Order.builder()
                .userId(userId)
                .shopId(201L)
                .paymentGroupId(paymentGroupId)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("150000"))
                .shippingAddress("456 Avenue")
                .paymentMethod("VNPAY")
                .build();
        orderRepository.save(order);

        // --- Lần 1: Thử thanh toán (Attempt 1) ---
        PaymentOrderData attempt1Data = new PaymentOrderData(paymentGroupId, new BigDecimal("150000"), "Lần 1", PaymentMethod.VNPAY);
        paymentGatewayService.generatePaymentUrl(attempt1Data, "127.0.0.1");

        List<Payment> paymentsAttempt1 = paymentRepository.findByPaymentGroupIdOrderByCreatedAtDesc(paymentGroupId);
        Payment payment1 = paymentsAttempt1.get(0);

        // Giả lập VNPay IPN báo thất bại (responseCode = 24 - Khách hàng hủy)
        Map<String, String> ipnParams1 = new HashMap<>();
        ipnParams1.put("vnp_TxnRef", payment1.getTransactionId());
        ipnParams1.put("vnp_ResponseCode", "24");
        ipnParams1.put("vnp_TransactionNo", "VNP_FAIL_1");
        ipnParams1.put("vnp_Amount", "15000000");

        paymentGatewayService.processVnPayIpn(ipnParams1);

        // Kiểm tra Lần 1
        Payment updatedPayment1 = paymentRepository.findById(payment1.getId()).orElseThrow();
        assertEquals(PaymentStatus.FAILED, updatedPayment1.getStatus());

        // ORDER VẪN GIỮ NGUYÊN PENDING (Không bị hủy!)
        Order orderAfterFail = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PENDING, orderAfterFail.getStatus());

        // Polling lúc này phải trả về FAILED (để giao diện hiện nút Thử lại)
        var statusAfterFail = paymentGatewayService.getPaymentGroupStatus(paymentGroupId);
        assertEquals("FAILED", statusAfterFail.getStatus());

        // --- Lần 2: Người dùng bấm "Thanh toán lại" (Retry - Attempt 2) ---
        PaymentOrderData attempt2Data = new PaymentOrderData(paymentGroupId, new BigDecimal("150000"), "Lần 2", PaymentMethod.VNPAY);
        paymentGatewayService.generatePaymentUrl(attempt2Data, "127.0.0.1");

        List<Payment> paymentsAttempt2 = paymentRepository.findByPaymentGroupIdOrderByCreatedAtDesc(paymentGroupId);
        assertEquals(2, paymentsAttempt2.size()); // 1:N Payment Group -> Multiple Payments!
        Payment payment2 = paymentsAttempt2.get(0); // Lần thử mới nhất

        // Giả lập VNPay IPN báo thành công cho Lần 2
        Map<String, String> ipnParams2 = new HashMap<>();
        ipnParams2.put("vnp_TxnRef", payment2.getTransactionId());
        ipnParams2.put("vnp_ResponseCode", "00");
        ipnParams2.put("vnp_TransactionNo", "VNP_SUCCESS_2");
        ipnParams2.put("vnp_Amount", "15000000");

        paymentGatewayService.processVnPayIpn(ipnParams2);

        // Kiểm tra Lần 2
        Payment updatedPayment2 = paymentRepository.findById(payment2.getId()).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, updatedPayment2.getStatus());

        // ORDER BÂY GIỜ ĐÃ SANG CONFIRMED
        Order orderAfterRetrySuccess = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, orderAfterRetrySuccess.getStatus());

        // Polling lúc này phải trả về SUCCESS
        var statusAfterRetrySuccess = paymentGatewayService.getPaymentGroupStatus(paymentGroupId);
        assertEquals("SUCCESS", statusAfterRetrySuccess.getStatus());
    }

    @Test
    @DisplayName("Scenario 3: Duplicate IPN (Idempotency Test)")
    void testDuplicateIpnIdempotency() {
        String paymentGroupId = UUID.randomUUID().toString();
        Long userId = 777L;

        Order order = Order.builder()
                .userId(userId)
                .shopId(301L)
                .paymentGroupId(paymentGroupId)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50000"))
                .shippingAddress("789 Road")
                .paymentMethod("VNPAY")
                .build();
        orderRepository.save(order);

        PaymentOrderData orderData = new PaymentOrderData(paymentGroupId, new BigDecimal("50000"), "Idempotency test", PaymentMethod.VNPAY);
        paymentGatewayService.generatePaymentUrl(orderData, "127.0.0.1");

        Payment payment = paymentRepository.findByPaymentGroupIdOrderByCreatedAtDesc(paymentGroupId).get(0);

        Map<String, String> ipnParams = new HashMap<>();
        ipnParams.put("vnp_TxnRef", payment.getTransactionId());
        ipnParams.put("vnp_ResponseCode", "00");
        ipnParams.put("vnp_TransactionNo", "VNP_DUP_123");
        ipnParams.put("vnp_Amount", "5000000");

        // Gọi IPN lần 1
        assertDoesNotThrow(() -> paymentGatewayService.processVnPayIpn(ipnParams));

        Payment firstResult = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, firstResult.getStatus());

        // Gọi IPN lần 2 (Duplicate Webhook call)
        assertDoesNotThrow(() -> paymentGatewayService.processVnPayIpn(ipnParams));

        Payment secondResult = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, secondResult.getStatus());
        
        // Order vẫn ở CONFIRMED
        Order finalOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, finalOrder.getStatus());
    }
}
