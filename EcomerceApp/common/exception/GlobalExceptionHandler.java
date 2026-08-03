package com.tmt.ecommerce.common.exception;

import com.tmt.ecommerce.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt lỗi liên quan đến Business Logic (Ví dụ: Trùng tên shop, user đã có shop)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(RuntimeException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage()) // Lấy câu thông báo từ lúc ta throw trong Service
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // 2. Bắt lỗi Validation (Khi @Valid ở Controller phát hiện dữ liệu truyền lên bị sai)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        // Lấy tất cả các field bị lỗi và thông báo lỗi tương ứng
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Dữ liệu đầu vào không hợp lệ")
                .data(errors) // Trả về danh sách lỗi cho Frontend hiển thị dưới từng ô input
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // 3. Bắt các lỗi chưa lường trước (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        // Trong thực tế sẽ dùng Logger để ghi lỗi này ra file log hệ thống
        System.err.println("Lỗi hệ thống: " + ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Hệ thống đang gặp sự cố, vui lòng thử lại sau.")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}