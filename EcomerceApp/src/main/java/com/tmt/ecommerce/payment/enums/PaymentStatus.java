package com.tmt.ecommerce.payment.enums;

public enum PaymentStatus {
    PENDING,  // Mới tạo URL, khách chưa trả tiền
    SUCCESS,  // Đã thanh toán thành công
    FAILED,   // Thanh toán thất bại (sai thẻ, hết tiền...)
    CANCELED  // Khách chủ động hủy
}