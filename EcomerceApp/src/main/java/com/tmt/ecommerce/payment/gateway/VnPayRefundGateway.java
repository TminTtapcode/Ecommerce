package com.tmt.ecommerce.payment.gateway;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.tmt.ecommerce.payment.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VnPayRefundGateway implements PaymentRefundGateway {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Value("${payment.vnpay.tmn-code}") private String tmnCode;
    @Value("${payment.vnpay.secret-key}") private String secretKey;
    @Value("${payment.vnpay.api-url}") private String apiUrl;
    @Value("${payment.vnpay.version}") private String version;
    @Value("${payment.vnpay.api-ip}") private String apiIp;

    @Override
    public RefundGatewayResult refund(RefundGatewayRequest request) {
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        String createDate = LocalDateTime.now(VN_ZONE).format(DATE);
        body.put("vnp_RequestId", request.merchantRequestId());
        body.put("vnp_Version", version);
        body.put("vnp_Command", "refund");
        body.put("vnp_TmnCode", tmnCode);
        body.put("vnp_TransactionType", request.fullRefund() ? "02" : "03");
        body.put("vnp_TxnRef", request.txnRef());
        body.put("vnp_Amount", request.amount().movePointRight(2).longValueExact() + "");
        body.put("vnp_TransactionNo", empty(request.originalGatewayTransactionNo()));
        body.put("vnp_TransactionDate", request.originalTransactionDate());
        body.put("vnp_CreateBy", request.createBy());
        body.put("vnp_CreateDate", createDate);
        body.put("vnp_IpAddr", request.serverIp());
        body.put("vnp_OrderInfo", request.orderInfo());
        body.put("vnp_SecureHash", HashUtil.hmacSha512(secretKey, String.join("|", body.values())));
        return sendAndMap(body);
    }

    @Override
    public RefundGatewayResult query(RefundGatewayRequest request) {
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        String createDate = LocalDateTime.now(VN_ZONE).format(DATE);
        body.put("vnp_RequestId", request.merchantRequestId());
        body.put("vnp_Version", version);
        body.put("vnp_Command", "querydr");
        body.put("vnp_TmnCode", tmnCode);
        body.put("vnp_TxnRef", request.txnRef());
        body.put("vnp_TransactionDate", request.originalTransactionDate());
        body.put("vnp_CreateDate", createDate);
        body.put("vnp_IpAddr", request.serverIp());
        body.put("vnp_OrderInfo", request.orderInfo());
        body.put("vnp_SecureHash", HashUtil.hmacSha512(secretKey, String.join("|", body.values())));
        return sendAndMap(body);
    }

    private RefundGatewayResult sendAndMap(Map<String, String> body) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) return unknown("HTTP_" + response.statusCode());
            Map<String, Object> raw = objectMapper.readValue(response.body(), new TypeReference<>() {});
            Map<String, String> values = new LinkedHashMap<>();
            raw.forEach((key, value) -> values.put(key, value == null ? "" : String.valueOf(value)));
            if (!verifyResponse(values)) return unknown("INVALID_SIGNATURE");
            return mapVerified(values);
        } catch (Exception ignored) {

            return unknown("TRANSPORT_OR_PARSE_ERROR");
        }
    }

    private boolean verifyResponse(Map<String, String> p) {
        String hash = p.get("vnp_SecureHash");
        if (hash == null || !"refund".equals(p.get("vnp_Command")) && !"querydr".equals(p.get("vnp_Command"))) return false;
        String data = String.join("|", empty(p.get("vnp_ResponseId")), empty(p.get("vnp_Command")),
                empty(p.get("vnp_ResponseCode")), empty(p.get("vnp_Message")), empty(p.get("vnp_TmnCode")),
                empty(p.get("vnp_TxnRef")), empty(p.get("vnp_Amount")), empty(p.get("vnp_BankCode")),
                empty(p.get("vnp_PayDate")), empty(p.get("vnp_TransactionNo")), empty(p.get("vnp_TransactionType")),
                empty(p.get("vnp_TransactionStatus")), empty(p.get("vnp_OrderInfo")));
        if ("querydr".equals(p.get("vnp_Command"))) {
            data = data + "|" + empty(p.get("vnp_PromotionCode")) + "|" + empty(p.get("vnp_PromotionAmount"));
        }
        return HashUtil.hmacSha512(secretKey, data).equalsIgnoreCase(hash);
    }

    private RefundGatewayResult mapVerified(Map<String, String> p) {
        String code = p.get("vnp_ResponseCode"), status = p.get("vnp_TransactionStatus");
        String metadata = "responseId=" + empty(p.get("vnp_ResponseId")) + ",message=" + empty(p.get("vnp_Message"));
        if ("00".equals(code) && "00".equals(status)
                && ("02".equals(p.get("vnp_TransactionType")) || "03".equals(p.get("vnp_TransactionType"))))
            return new RefundGatewayResult(RefundGatewayResult.Outcome.CONFIRMED, code, status, p.get("vnp_TransactionNo"), metadata);
        if ("09".equals(status) || "95".equals(code) || "02".equals(code) || "03".equals(code)
                || "91".equals(code) || "97".equals(code))
            return new RefundGatewayResult(RefundGatewayResult.Outcome.REJECTED, code, status, p.get("vnp_TransactionNo"), metadata);

        return new RefundGatewayResult(RefundGatewayResult.Outcome.UNKNOWN, code, status, p.get("vnp_TransactionNo"), metadata);
    }

    private RefundGatewayResult unknown(String detail) {
        return new RefundGatewayResult(RefundGatewayResult.Outcome.UNKNOWN, null, null, null, "outcome=" + detail);
    }
    private static String empty(String value) { return value == null ? "" : value; }
}
