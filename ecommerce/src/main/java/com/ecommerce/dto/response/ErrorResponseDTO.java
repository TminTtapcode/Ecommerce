package com.ecommerce.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;       // Mã HTTP (400, 401, 404, 500)
    private String error;     // Tên lỗi ngắn gọn
    private String message;   // Thông báo chi tiết cho user đọc
    private String path;      // Đường dẫn API bị lỗi
}