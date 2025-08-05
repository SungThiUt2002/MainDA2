package com.stu.account_service.exception;

import com.stu.account_service.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ========== BUSINESS LOGIC EXCEPTIONS ==========
    //Appexxception được định nghĩa
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(
            AppException ex, HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();
        log.warn("Application exception [{}]: {}", errorCode.getCode(), ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .data(request.getRequestURI())
                .message(ex.getMessage())
                .code(errorCode.getCode())
                .build();
        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(response);
    }

    // ========== VALIDATION EXCEPTIONS ==========

    //Lỗi từ @Valid trong DTO – kiểm tra từng field, trả về danh sách lỗi.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        // Check if validation message is an ErrorCode
        String enumKey = ex.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
            // If it's a known error code, use it directly
            ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                    .code(errorCode.getCode())
                    .message(errorCode.getMessage())
                    .timestamp(LocalDateTime.now())
                    .path(request.getRequestURI())
                    .build();

            return ResponseEntity.status(errorCode.getStatusCode()).body(response);

        } catch (IllegalArgumentException e) {
            // If not an error code, collect field validation errors
            ex.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });

            log.warn("Validation errors: {}", errors);

            ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                    .code(ErrorCode.INVALID_KEY.getCode())
                    .message("Validation failed")
                    .data(errors)
                    .timestamp(LocalDateTime.now())
                    .path(request.getRequestURI())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    //Lỗi từ @Validated (ràng buộc ở method-level).
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Constraint violation errors: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(ErrorCode.INVALID_KEY.getCode())
                .message("Validation failed")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }


    //Lỗi khi binding dữ liệu request vào DTO thất bại (thường với @ModelAttribute hoặc @RequestParam).
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(
            BindException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Binding errors: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(ErrorCode.INVALID_KEY.getCode())
                .message("Binding failed")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // ========== HTTP & WEB EXCEPTIONS ==========

    // Dùng sai HTTP method (POST thay vì GET…).
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("Method not supported: {} for {}", ex.getMethod(), request.getRequestURI());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(9003)
                .message("HTTP method not supported: " + ex.getMethod())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
//Gửi content-type không hỗ trợ.
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        log.warn("Media type not supported: {}", ex.getContentType());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(9004)
                .message("Media type not supported: " + ex.getContentType())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    //Gửi JSON sai định dạng.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Message not readable: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(9005)
                .message("Invalid JSON format or request body")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    //Thiếu tham số trong request URL.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Missing parameter: {}", ex.getParameterName());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(9006)
                .message("Missing required parameter: " + ex.getParameterName())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    //Truyền sai kiểu dữ liệu trong param.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Type mismatch for parameter: {}", ex.getName());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(9007)
                .message("Invalid type for parameter: " + ex.getName())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // ========== SECURITY EXCEPTIONS ==========

    //Không đủ quyền truy cập.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .data(ErrorCode.UNAUTHORIZED)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED.getStatusCode())
                .body(response);
    }

    //Chưa đăng nhập hoặc token sai.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("Authentication failed: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .data(ErrorCode.UNAUTHENTICATED)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity
                .status(ErrorCode.UNAUTHENTICATED.getStatusCode())
                .body(response);
    }

    // Sai mật khẩu.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {

        log.warn("Bad credentials: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .data(ErrorCode.UNAUTHENTICATED)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    //Các trạng thái tài khoản bị khoá, vô hiệu hoá…
    @ExceptionHandler(AccountExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountExpiredException(
            AccountExpiredException ex, HttpServletRequest request) {

        log.warn("Account expired: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(2008)
                .message("Account has expired")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleLockedException(
            LockedException ex, HttpServletRequest request) {

        log.warn("Account locked: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(2009)
                .message("Account is locked")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.LOCKED).body(response);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleDisabledException(
            DisabledException ex, HttpServletRequest request) {

        log.warn("Account disabled: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(2010)
                .message("Account is disabled")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ========== DATABASE EXCEPTIONS ==========

    //Lỗi chung khi truy cập DB.
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request) {

        log.error("Data access error: ", ex);

        ApiResponse<Object> response = ApiResponse.builder()
                .data(ErrorCode.DATA_ACCESS_ERROR)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity
                .status(ErrorCode.DATA_ACCESS_ERROR.getStatusCode())
                .body(response);
    }

    //Vi phạm ràng buộc DB
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation: ", ex);

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(5006)
                .message("Data integrity constraint violation")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    //Lỗi khi xử lý dữ liệu đồng thời.
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLockingException(
            OptimisticLockingFailureException ex, HttpServletRequest request) {

        log.warn("Optimistic locking failure: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .data(ErrorCode.CONCURRENT_MODIFICATION)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handlePessimisticLockingException(
            PessimisticLockingFailureException ex, HttpServletRequest request) {

        log.warn("Pessimistic locking failure: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(5007)
                .message("Resource is currently locked, please try again later")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.LOCKED).body(response);
    }

    // ========== FILE & IO EXCEPTIONS ==========

    //Không tìm thấy file.
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileNotFoundException(
            FileNotFoundException ex, HttpServletRequest request) {

        log.warn("File not found: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(7001)
                .message("File not found")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    //Upload vượt quá giới hạn.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        log.warn("File size exceeded: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(7002)
                .message("File size exceeded maximum limit")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    //Lỗi nhập/xuất dữ liệu.
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Object>> handleIOException(
            IOException ex, HttpServletRequest request) {

        log.error("IO error: ", ex);

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(7003)
                .message("IO operation failed")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    //Gọi timeout.
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiResponse<Object>> handleTimeoutException(
            TimeoutException ex, HttpServletRequest request) {

        log.warn("Timeout occurred: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(8001)
                .message("Request timeout")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    // ========== RUNTIME EXCEPTIONS ==========

    //Truyền đối số không hợp lệ.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Illegal argument: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(9008)
                .message("Invalid argument: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    //Trạng thái không hợp lệ để thực hiện logic.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {

        log.warn("Illegal state: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(9009)
                .message("Invalid operation state: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Gọi hành động chưa hỗ trợ.
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnsupportedOperationException(
            UnsupportedOperationException ex, HttpServletRequest request) {

        log.warn("Unsupported operation: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(9010)
                .message("Operation not supported")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }

    // ========== FALLBACK EXCEPTION ==========

    //Bắt tất cả lỗi chưa xác định – trả về mã lỗi tổng quát 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error: ", ex);

        ApiResponse<Object> response = ApiResponse.builder()
                .data(ErrorCode.UNCATEGORIZED_EXCEPTION)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}