package com.tmt.ecommerce.payment.controller;

import com.tmt.ecommerce.payment.dto.request.PaymentCreateRequest;
import com.tmt.ecommerce.payment.dto.response.PaymentCreateResponse;
import com.tmt.ecommerce.payment.dto.response.PaymentGroupStatusResponse;
import com.tmt.ecommerce.common.annotation.CurrentUserId;
import com.tmt.ecommerce.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import com.tmt.ecommerce.payment.dto.request.PaymentOrderData;
import com.tmt.ecommerce.payment.service.PaymentGatewayService;

import com.tmt.ecommerce.order.api.OrderInternalService;
import com.tmt.ecommerce.order.api.dto.OrderPaymentDto;
import com.tmt.ecommerce.payment.enums.PaymentMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    // SỬA Ở ĐÂY: Dùng Interface
    private final OrderInternalService orderInternalService;
    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> createPaymentUrl(
            @CurrentUserId Long userId,
            @Valid @RequestBody PaymentCreateRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);

        // Gọi qua Interface để validate quyền và trạng thái nhóm đơn hàng
        orderInternalService.validateGroupOwnershipAndStatus(request.paymentGroupId(), userId);
        OrderPaymentDto orderData = orderInternalService.getPaymentDataForGroup(request.paymentGroupId());

        PaymentMethod method = PaymentMethod.valueOf(orderData.method());
        PaymentOrderData paymentData = new PaymentOrderData(
                orderData.paymentGroupId(),
                orderData.amount(),
                orderData.description(),
                method
        );

        String paymentUrl = paymentGatewayService.generatePaymentUrl(paymentData, clientIp);

        ApiResponse<PaymentCreateResponse> response = ApiResponse.<PaymentCreateResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Tạo URL thanh toán thành công")
                .data(new PaymentCreateResponse(paymentUrl))
                .build();

        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<?> vnpayIpn(@RequestParam Map<String, String> queryParams) {
        try {
            // 1. Pass toàn bộ tham số vào Service để xử lý nghiệp vụ
            paymentGatewayService.processVnPayIpn(queryParams);

            // 2. Trả về format chuẩn VNPAY yêu cầu để họ biết mình đã nhận được
            return ResponseEntity.ok(Map.of(
                    "RspCode", "00",
                    "Message", "Confirm Success"
            ));
        } catch (Exception e) {
            // Log lỗi và báo cho VNPAY biết hệ thống đang lỗi
            return ResponseEntity.ok(Map.of(
                    "RspCode", "99",
                    "Message", "Unknown error"
            ));
        }
    }

    @GetMapping("/group/{paymentGroupId}/status")
    public ResponseEntity<ApiResponse<PaymentGroupStatusResponse>> getPaymentGroupStatus(
            @PathVariable String paymentGroupId) {

        PaymentGroupStatusResponse groupStatus = paymentGatewayService.getPaymentGroupStatus(paymentGroupId);

        ApiResponse<PaymentGroupStatusResponse> response = ApiResponse.<PaymentGroupStatusResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy trạng thái thanh toán nhóm đơn hàng thành công")
                .data(groupStatus)
                .build();

        return ResponseEntity.ok(response);
    }
}