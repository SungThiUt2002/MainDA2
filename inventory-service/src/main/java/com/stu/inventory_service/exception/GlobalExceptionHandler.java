package com.stu.inventory_service.exception;

import com.stu.inventory_service.dto.response.ApiResponse;
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
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(
            AppException ex, HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();
        log.warn("Application exception [{}]: {}", errorCode.getCode(), ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .result(request.getRequestURI())
                .message(ex.getMessage())
                .code(errorCode.getCode())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(response);
    }

    // ========== VALIDATION EXCEPTIONS ==========
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation errors: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message("Validation failed")
                .result(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

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
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message("Validation failed")
                .result(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

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
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message("Binding failed")
                .result(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // ========== HTTP & WEB EXCEPTIONS ==========
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("Method not supported: {} for {}", ex.getMethod(), request.getRequestURI());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.HTTP_METHOD_NOT_SUPPORTED.getCode())
                .message("HTTP method not supported: " + ex.getMethod())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        log.warn("Media type not supported: {}", ex.getContentType());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED.getCode())
                .message("Media type not supported: " + ex.getContentType())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Message not readable: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.INVALID_JSON_FORMAT.getCode())
                .message("Invalid JSON format or request body")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Missing parameter: {}", ex.getParameterName());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.MISSING_PARAMETER.getCode())
                .message("Missing required parameter: " + ex.getParameterName())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Type mismatch for parameter: {}", ex.getName());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.INVALID_PARAMETER_TYPE.getCode())
                .message("Invalid type for parameter: " + ex.getName())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }



    // ========== DATABASE EXCEPTIONS ==========
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request) {

        log.error("Data access error: ", ex);

        ApiResponse<Object> response = ApiResponse.builder()
                .result(request.getRequestURI())
                .message("Data access error")
                .code(ErrorCode.DATA_ACCESS_ERROR.getCode())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation: ", ex);

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.DATA_INTEGRITY_VIOLATION.getCode())
                .message("Data integrity constraint violation")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLockingException(
            OptimisticLockingFailureException ex, HttpServletRequest request) {

        log.warn("Optimistic locking failure: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .result(request.getRequestURI())
                .message("Concurrent modification detected, please retry")
                .code(ErrorCode.CONCURRENT_MODIFICATION.getCode())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handlePessimisticLockingException(
            PessimisticLockingFailureException ex, HttpServletRequest request) {

        log.warn("Pessimistic locking failure: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.RESOURCE_LOCKED.getCode())
                .message("Resource is currently locked, please try again later")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.LOCKED).body(response);
    }

    // ========== FILE & IO EXCEPTIONS ==========
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileNotFoundException(
            FileNotFoundException ex, HttpServletRequest request) {

        log.warn("File not found: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.FILE_NOT_FOUND.getCode())
                .message("File not found")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        log.warn("File size exceeded: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.FILE_SIZE_EXCEEDED.getCode())
                .message("File size exceeded maximum limit")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Object>> handleIOException(
            IOException ex, HttpServletRequest request) {

        log.error("IO error: ", ex);

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.IO_OPERATION_FAILED.getCode())
                .message("IO operation failed")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiResponse<Object>> handleTimeoutException(
            TimeoutException ex, HttpServletRequest request) {

        log.warn("Timeout occurred: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.REQUEST_TIMEOUT.getCode())
                .message("Request timeout")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    // ========== RUNTIME EXCEPTIONS ==========
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Illegal argument: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.ILLEGAL_ARGUMENT.getCode())
                .message("Invalid argument: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {

        log.warn("Illegal state: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.ILLEGAL_STATE.getCode())
                .message("Invalid operation state: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnsupportedOperationException(
            UnsupportedOperationException ex, HttpServletRequest request) {

        log.warn("Unsupported operation: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ErrorCode.UNSUPPORTED_OPERATION.getCode())
                .message("Operation not supported")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }

    // ========== FALLBACK EXCEPTION ==========
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error: ", ex);

        ApiResponse<Object> response = ApiResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message("An unexpected error occurred")
                .result(request.getRequestURI())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
} 