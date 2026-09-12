package com.tmt.ecommerce.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Yêu cầu đăng nhập"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Email đã được sử dụng"),

    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy gian hàng"),
    SHOP_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Bạn đã sở hữu gian hàng rồi"),
    SHOP_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "Tên gian hàng đã tồn tại"),
    SHOP_NOT_ACTIVE(HttpStatus.FORBIDDEN, "Gian hàng chưa được kích hoạt hoặc đã bị khóa"),
    SHOP_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Chuyển đổi trạng thái gian hàng không hợp lệ"),

    SHOP_PRIOR_STATUS_MISSING(HttpStatus.CONFLICT, "Không thể tự động mở khóa; cần đối soát trạng thái trước khi khóa"),

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"),
    VARIANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy biến thể sản phẩm"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "Số lượng tồn kho không đủ"),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"),
    ORDER_STATE_CONFLICT(HttpStatus.CONFLICT, "Trạng thái đơn hàng đã thay đổi; vui lòng tải lại"),
    ORDER_PAYMENT_CONDITION_NOT_MET(HttpStatus.CONFLICT, "Đơn hàng không đáp ứng điều kiện thanh toán cho thao tác này"),
    INVALID_ORDER_STATE_TRANSITION(HttpStatus.BAD_REQUEST, "Chuyển đổi trạng thái đơn hàng không hợp lệ"),

    REFUND_NOT_ACTIONABLE(HttpStatus.CONFLICT, "Refund is not actionable"),
    REFUND_IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "Idempotency key belongs to a different refund command"),
    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "Refund was not found"),
    REFUND_GATEWAY_UNRESOLVED(HttpStatus.CONFLICT, "Refund outcome requires reconciliation"),

    VOUCHER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy mã giảm giá"),
    VOUCHER_EXPIRED(HttpStatus.BAD_REQUEST, "Mã giảm giá đã hết hạn"),
    VOUCHER_USAGE_LIMIT_REACHED(HttpStatus.BAD_REQUEST, "Mã giảm giá đã hết lượt sử dụng"),
    VOUCHER_MIN_ORDER_NOT_MET(HttpStatus.BAD_REQUEST, "Đơn hàng chưa đạt giá trị tối thiểu"),

    NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức không được hỗ trợ"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Định dạng nội dung không được hỗ trợ"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Định dạng phản hồi không được hỗ trợ"),
    REQUEST_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "Nội dung yêu cầu quá lớn"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống đang gặp sự cố, vui lòng thử lại sau");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    public static ErrorCode forHttpStatus(int status) {
        return switch (status) {
            case 401 -> UNAUTHORIZED;
            case 403 -> ACCESS_DENIED;
            case 404 -> NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 406 -> NOT_ACCEPTABLE;
            case 413 -> REQUEST_TOO_LARGE;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            default -> status >= 500 ? INTERNAL_SERVER_ERROR : INVALID_INPUT;
        };
    }

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
