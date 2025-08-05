package com.stu.shipping_service.exception;

import com.stu.shipping_service.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Xử lý exception toàn cục cho Shipping Service, trả về lỗi chuẩn hóa cho client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Xử lý các exception nghiệp vụ (ShippingException) do service/business logic ném ra.
     * Trả về mã lỗi, message, httpStatus tương ứng với ErrorCode.
     */
    @ExceptionHandler(ShippingException.class)
    public ResponseEntity<ApiResponse<Object>> handleShippingException(ShippingException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(errorCode.getCode())
                .message(ex.getMessage())
                .data(null)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus().value()).body(response);
    }

    /**
     * Xử lý lỗi validate @Valid ở request body (ví dụ: thiếu trường, sai format, ...).
     * Trả về mã lỗi VALIDATION_ERROR, message tổng hợp các lỗi field.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ApiResponse<?> response = ApiResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus().value()).body(response);
    }

    /**
     * Xử lý lỗi validate ở @RequestParam, @PathVariable, ... (ConstraintViolationException).
     * Trả về mã lỗi VALIDATION_ERROR, message là lỗi validate.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus().value()).body(response);
    }

    /**
     * Xử lý lỗi sai kiểu dữ liệu ở @PathVariable, @RequestParam (ví dụ: truyền id=abc thay vì id=1).
     * Trả về mã lỗi VALIDATION_ERROR, message là lỗi type mismatch.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus().value()).body(response);
    }

    /**
     * Xử lý tất cả các exception còn lại (lỗi hệ thống, lỗi không xác định).
     * Trả về mã lỗi INTERNAL_SERVER_ERROR, message mặc định.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleOther(Exception ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message("Internal server error")
                .data(null)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
} 