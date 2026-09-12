package com.tmt.ecommerce.payment.gateway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.tmt.ecommerce.payment.util.HashUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnPayRefundGatewayTest {
    private static final String SECRET = "sandbox-test-secret";
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void verifiedPartialRefundResponseIsConfirmedAndUsesOfficialRefundCommand() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        start(exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, signedResponse("refund", "00", "00", "03"));
        });

        RefundGatewayResult result = gateway().refund(request());

        assertEquals(RefundGatewayResult.Outcome.CONFIRMED, result.outcome());
        assertEquals("REFUND_GATEWAY_TXN", result.refundTransactionNo());
        assertTrue(requestBody.get().contains("\"vnp_Command\":\"refund\""));
        assertTrue(requestBody.get().contains("\"vnp_TransactionType\":\"03\""));
    }

    @Test
    void invalidResponseSignatureIsUnknownRatherThanAConfirmedRefund() throws Exception {
        start(exchange -> respond(exchange, "{\"vnp_Command\":\"refund\",\"vnp_SecureHash\":\"not-valid\"}"));

        assertEquals(RefundGatewayResult.Outcome.UNKNOWN, gateway().refund(request()).outcome());
    }

    @Test
    void querydrPaymentResponseNeverConfirmsRefundWithoutRefundTransactionEvidence() throws Exception {
        start(exchange -> respond(exchange, signedResponse("querydr", "00", "00", "01")));

        assertEquals(RefundGatewayResult.Outcome.UNKNOWN, gateway().query(request()).outcome());
    }

    private VnPayRefundGateway gateway() {
        VnPayRefundGateway gateway = new VnPayRefundGateway(new ObjectMapper());
        ReflectionTestUtils.setField(gateway, "tmnCode", "TESTCODE");
        ReflectionTestUtils.setField(gateway, "secretKey", SECRET);
        ReflectionTestUtils.setField(gateway, "apiUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/transaction");
        ReflectionTestUtils.setField(gateway, "version", "2.1.0");
        ReflectionTestUtils.setField(gateway, "apiIp", "127.0.0.1");
        return gateway;
    }

    private RefundGatewayRequest request() {
        return new RefundGatewayRequest("REQ123", "PAY123", "ORIGINAL123", "20260910120000",
                new BigDecimal("100.00"), "ADMIN1", "127.0.0.1", "Refund order 1", false);
    }

    private void start(ExchangeHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/transaction", handler::handle);
        server.start();
    }

    private static String signedResponse(String command, String responseCode, String transactionStatus,
                                         String transactionType) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("vnp_ResponseId", "RESPONSE123");
        p.put("vnp_Command", command);
        p.put("vnp_ResponseCode", responseCode);
        p.put("vnp_Message", "Success");
        p.put("vnp_TmnCode", "TESTCODE");
        p.put("vnp_TxnRef", "PAY123");
        p.put("vnp_Amount", "10000");
        p.put("vnp_BankCode", "NCB");
        p.put("vnp_PayDate", "20260910121000");
        p.put("vnp_TransactionNo", "REFUND_GATEWAY_TXN");
        p.put("vnp_TransactionType", transactionType);
        p.put("vnp_TransactionStatus", transactionStatus);
        p.put("vnp_OrderInfo", "Refund order 1");
        if ("querydr".equals(command)) {
            p.put("vnp_PromotionCode", "");
            p.put("vnp_PromotionAmount", "");
        }
        String data = String.join("|", p.get("vnp_ResponseId"), p.get("vnp_Command"), p.get("vnp_ResponseCode"),
                p.get("vnp_Message"), p.get("vnp_TmnCode"), p.get("vnp_TxnRef"), p.get("vnp_Amount"),
                p.get("vnp_BankCode"), p.get("vnp_PayDate"), p.get("vnp_TransactionNo"),
                p.get("vnp_TransactionType"), p.get("vnp_TransactionStatus"), p.get("vnp_OrderInfo"));
        if ("querydr".equals(command)) data += "|" + p.get("vnp_PromotionCode") + "|" + p.get("vnp_PromotionAmount");
        p.put("vnp_SecureHash", HashUtil.hmacSha512(SECRET, data));
        return p.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                .collect(java.util.stream.Collectors.joining(",", "{", "}"));
    }

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @FunctionalInterface
    private interface ExchangeHandler { void handle(HttpExchange exchange) throws IOException; }
}
