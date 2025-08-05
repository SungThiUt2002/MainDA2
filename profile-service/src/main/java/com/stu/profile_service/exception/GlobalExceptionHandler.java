package com.stu.profile_service.exception;


import com.stu.profile_service.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Xử lý AppException (lỗi nghiệp vụ custom)
    @ExceptionHandler(AppException.class)
    public ApiResponse<Object> handleAppException(AppException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("Application exception [{}]: {}", errorCode.getCode(), ex.getMessage());
        return ApiResponse.builder()
                .code(errorCode.getCode())
                .message(ex.getMessage())
                .data(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
    }

    // Xử lý lỗi validate @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation errors: {}", errors);
        return ApiResponse.<Map<String, String>>builder()
                .code(ErrorCode.INVALID_ADDRESS.getCode())
                .message("Validation failed")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
    }

    // Xử lý lỗi JSON không hợp lệ
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Object> handleMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return ApiResponse.builder()
                .code(ErrorCode.INVALID_ADDRESS.getCode())
                .message("Malformed JSON request")
                .data(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
    }

    // Xử lý các lỗi không xác định khác
    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ApiResponse.builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message("Internal server error")
                .data(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
    }
}
