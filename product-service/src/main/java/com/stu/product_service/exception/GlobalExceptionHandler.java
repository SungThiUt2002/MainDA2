package com.stu.product_service.exception;

import com.stu.product_service.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import java.util.stream.Collectors;

/**
 * Xử lý exception toàn cục cho Product Service, trả về lỗi chuẩn hóa cho client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleProductServiceException(ProductServiceException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(errorCode.getCode())
                .message(ex.getMessage())
                .data(null)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(errorCode.getStatusCode().value()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatusCode().value())
                .body(ApiResponse.builder()
                        .code(ErrorCode.VALIDATION_FAILED.getCode())
                        .message(message)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.INTERNAL_ERROR.getCode())
                .message("Internal server error")
                .data(null)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
} 