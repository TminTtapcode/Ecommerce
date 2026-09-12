package com.tmt.ecommerce.payment.strategy;

import com.tmt.ecommerce.payment.dto.request.PaymentOrderData;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.util.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VnPayStrategy implements PaymentStrategy {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Value("${payment.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${payment.vnpay.secret-key}")
    private String secretKey;

    @Value("${payment.vnpay.pay-url}")
    private String vnpPayUrl;

    @Value("${payment.vnpay.return-url}")
    private String returnUrl;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public String createPaymentUrl(PaymentOrderData orderData, String ipAddress, String txnRef) {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_ReturnUrl", returnUrl);

        BigDecimal amountVnPay = orderData.amount().multiply(new BigDecimal("100"));
        vnpParams.put("vnp_Amount", String.valueOf(amountVnPay.longValue()));

        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderData.description());
        vnpParams.put("vnp_IpAddr", ipAddress);

        LocalDateTime now = orderData.vnpayTransactionDate() == null
                ? LocalDateTime.now(VN_ZONE)
                : LocalDateTime.parse(orderData.vnpayTransactionDate(), FORMATTER);
        vnpParams.put("vnp_CreateDate", now.format(FORMATTER));
        vnpParams.put("vnp_ExpireDate", now.plusMinutes(15).format(FORMATTER));

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {

                hashData.append(fieldName).append("=").append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }

        String vnpSecureHash = HashUtil.hmacSha512(secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);

        return vnpPayUrl + "?" + query.toString();
    }

    @Override
    public boolean verifyIpnSignature(Map<String, String> params) {
        try {

            String vnp_SecureHash = params.get("vnp_SecureHash");
            if (vnp_SecureHash == null) {
                return false;
            }

            Map<String, String> hashParams = new HashMap<>(params);
            hashParams.remove("vnp_SecureHash");
            hashParams.remove("vnp_SecureHashType");

            List<String> fieldNames = new ArrayList<>(hashParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            for (String fieldName : fieldNames) {
                String fieldValue = hashParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                        hashData.append("&");
                    }
                }
            }

            String calculatedHash = HashUtil.hmacSha512(secretKey, hashData.toString());

            return calculatedHash.equalsIgnoreCase(vnp_SecureHash);

        } catch (Exception e) {
            return false;
        }
    }

}
