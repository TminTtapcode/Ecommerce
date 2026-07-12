package com.ecommerce.exception;

import com.ecommerce.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt lỗi người dùng nhập sai dữ liệu (do @Valid ném ra)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        // Gom tất cả các lỗi của các field lại thành 1 chuỗi message
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()) // HTTP 400
                .error("Validation Error")
                .message(errors.toString()) // Thực tế enterprise sẽ trả List/Map thay vì String, nhưng tạm làm vậy cho đơn giản
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 2. Bắt lỗi sai email hoặc mật khẩu (Spring Security ném ra)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value()) // HTTP 401
                .error("Unauthorized")
                .message("Email hoặc mật khẩu không chính xác")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // 3. Bắt CÁC LỖI CÒN LẠI (Exception chung) để không bao giờ rớt HTML ra ngoài
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // HTTP 500
                .error("Internal Server Error")
                .message("Hệ thống đang gặp sự cố, vui lòng thử lại sau.") // Giấu stacktrace đi!
                .path(request.getRequestURI())
                .build();

        // Trong môi trường Enterprise, chỗ này sẽ phải gọi thư viện log (như Slf4j) 
        // để ghi cái ex.getMessage() ra file log cho Dev đọc.
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}