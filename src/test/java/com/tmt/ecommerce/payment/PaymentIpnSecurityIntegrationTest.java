package com.tmt.ecommerce.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentIpnSecurityIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentStrategyFactory paymentStrategyFactory;

    @MockitoBean(name = "vnPayStrategy")
    private PaymentStrategy mockVnPayStrategy;

    private HttpClient httpClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        
        when(paymentStrategyFactory.getStrategy(PaymentMethod.VNPAY)).thenReturn(mockVnPayStrategy);
        when(mockVnPayStrategy.createPaymentUrl(any(), any(), any())).thenReturn("http://vnpay.sandbox.vn/pay");
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/payments/vnpay/ipn";
    }

    @Test
    @DisplayName("1. IPN without JWT -> should be accepted (200 OK) without 401 Unauthorized")
    void testIpnWithoutJwt() throws Exception {
        when(mockVnPayStrategy.verifyIpnSignature(any())).thenReturn(true);

        String url = UriComponentsBuilder.fromUriString(getBaseUrl())
                .queryParam("vnp_TxnRef", "NON_EXISTENT")
                .queryParam("vnp_ResponseCode", "00")
                .queryParam("vnp_SecureHash", "dummy_hash")
                .toUriString();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Map<String, String> body = objectMapper.readValue(response.body(), Map.class);
        assertEquals("99", body.get("RspCode"));
    }

    @Test
    @DisplayName("2. Invalid signature -> should reject and not update status")
    void testInvalidSignature() throws Exception {
        when(mockVnPayStrategy.verifyIpnSignature(any())).thenReturn(false);

        String url = UriComponentsBuilder.fromUriString(getBaseUrl())
                .queryParam("vnp_TxnRef", "SOME_TXN")
                .queryParam("vnp_SecureHash", "invalid_hash")
                .toUriString();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Map<String, String> body = objectMapper.readValue(response.body(), Map.class);
        assertEquals("99", body.get("RspCode"));
    }

    @Test
    @DisplayName("3. Unknown transaction -> should handle gracefully without 500 error")
    void testUnknownTransaction() throws Exception {
        when(mockVnPayStrategy.verifyIpnSignature(any())).thenReturn(true);

        String url = UriComponentsBuilder.fromUriString(getBaseUrl())
                .queryParam("vnp_TxnRef", "SOME_UNKNOWN_TXN")
                .queryParam("vnp_SecureHash", "valid_hash")
                .toUriString();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Map<String, String> body = objectMapper.readValue(response.body(), Map.class);
        assertEquals("99", body.get("RspCode"));
    }

    @Test
    @DisplayName("4. Duplicate IPN -> idempotent, should not fail or duplicate updates")
    void testDuplicateIpnIdempotent() throws Exception {
        when(mockVnPayStrategy.verifyIpnSignature(any())).thenReturn(true);

        String paymentGroupId = UUID.randomUUID().toString();

        Order order = Order.builder()
                .userId(1L)
                .shopId(1L)
                .paymentGroupId(paymentGroupId)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100000"))
                .shippingAddress("123 Street")
                .paymentMethod("VNPAY")
                .build();
        orderRepository.save(order);

        PaymentOrderData orderData = new PaymentOrderData(paymentGroupId, new BigDecimal("100000"), "Test", PaymentMethod.VNPAY);
        paymentGatewayService.generatePaymentUrl(orderData, "127.0.0.1");

        Payment payment = paymentRepository.findByPaymentGroupIdOrderByCreatedAtDesc(paymentGroupId).get(0);
        String txnRef = payment.getTransactionId();

        // First IPN call
        String url1 = UriComponentsBuilder.fromUriString(getBaseUrl())
                .queryParam("vnp_TxnRef", txnRef)
                .queryParam("vnp_ResponseCode", "00")
                .queryParam("vnp_TransactionNo", "VNP_123")
                .queryParam("vnp_Amount", "10000000")
                .queryParam("vnp_SecureHash", "valid_hash")
                .toUriString();

        HttpRequest request1 = HttpRequest.newBuilder().uri(URI.create(url1)).GET().build();
        HttpResponse<String> response1 = httpClient.send(request1, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response1.statusCode());
        Map<String, String> body1 = objectMapper.readValue(response1.body(), Map.class);
        assertEquals("00", body1.get("RspCode"));

        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, updatedPayment.getStatus());

        // Second IPN call (Duplicate)
        HttpRequest request2 = HttpRequest.newBuilder().uri(URI.create(url1)).GET().build();
        HttpResponse<String> response2 = httpClient.send(request2, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response2.statusCode());
        Map<String, String> body2 = objectMapper.readValue(response2.body(), Map.class);
        assertEquals("00", body2.get("RspCode")); // Should still return 00

        // Verify state is still SUCCESS
        Payment finalPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, finalPayment.getStatus());
    }
}
