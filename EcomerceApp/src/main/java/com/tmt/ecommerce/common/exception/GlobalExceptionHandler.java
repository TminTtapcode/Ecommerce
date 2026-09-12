package com.tmt.ecommerce.common.exception;

import com.tmt.ecommerce.common.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return response(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, BusinessException.class})
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(RuntimeException ex) {
        return response(ErrorCode.INVALID_INPUT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error ->
                errors.putIfAbsent(error instanceof FieldError field ? field.getField() : "_global",
                        error.getDefaultMessage() != null ? error.getDefaultMessage() : "Dữ liệu không hợp lệ"));
        return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                .status(400)
                .errorCode(ErrorCode.INVALID_INPUT.name())
                .message(ErrorCode.INVALID_INPUT.getDefaultMessage())
                .fieldErrors(errors)
                .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return response(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getDefaultMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return response(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getDefaultMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return response(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getDefaultMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return response(ErrorCode.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {

        if (ex instanceof ErrorResponse error) {
            int status = error.getStatusCode().value();
            ErrorCode code = ErrorCode.forHttpStatus(status);
            return ResponseEntity.status(status).headers(error.getHeaders())
                    .body(ApiResponse.error(status, code, code.getDefaultMessage()));
        }
        ResponseStatus annotated = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if (annotated != null) {
            int status = annotated.code().value();
            ErrorCode code = ErrorCode.forHttpStatus(status);
            return ResponseEntity.status(status).body(ApiResponse.error(status, code, code.getDefaultMessage()));
        }

        if (ex instanceof org.springframework.http.converter.HttpMessageNotReadableException
                || ex instanceof org.springframework.beans.TypeMismatchException) {
            return response(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getDefaultMessage());
        }
        log.error("Unhandled API exception", ex);
        return response(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage());
    }

    private ResponseEntity<ApiResponse<Void>> response(ErrorCode code, String message) {
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.error(code.getHttpStatus().value(), code, message));
    }
}
