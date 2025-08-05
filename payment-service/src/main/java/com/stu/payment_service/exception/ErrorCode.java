package com.stu.payment_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;

/**
 * Định nghĩa các mã lỗi nghiệp vụ và hệ thống cho Payment Service.
 */
@Getter
public enum ErrorCode {
    // Auth errors
    UNAUTHENTICATED(6001, "Authentication required", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(6002, "Access denied", HttpStatus.FORBIDDEN),

    // Payment errors (6100-6199)
    PAYMENT_ALREADY_EXISTS(6100, "Đơn hàng đã có giao dịch thanh toán.", HttpStatus.CONFLICT),
    PAYMENT_NOT_FOUND(6101, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_STATUS_INVALID(6102, "Invalid payment status", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_CONFIRMED(6103, "Payment already confirmed", HttpStatus.CONFLICT),
    PAYMENT_GATEWAY_ERROR(6104, "Payment gateway error", HttpStatus.BAD_GATEWAY),
    PAYMENT_CANCELLED(6105, "Payment has been cancelled", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_INVALID(6106, "Invalid payment amount", HttpStatus.BAD_REQUEST),
    PAYMENT_METHOD_UNSUPPORTED(6107, "Unsupported payment method", HttpStatus.BAD_REQUEST),

    // Refund errors (6200-6299)
    REFUND_NOT_FOUND(6201, "Refund not found", HttpStatus.NOT_FOUND),
    REFUND_STATUS_INVALID(6202, "Invalid refund status", HttpStatus.BAD_REQUEST),
    REFUND_AMOUNT_EXCEEDS(6203, "Refund amount exceeds payment amount", HttpStatus.BAD_REQUEST),
    REFUND_ALREADY_PROCESSED(6204, "Refund already processed", HttpStatus.CONFLICT),
    REFUND_GATEWAY_ERROR(6205, "Refund gateway error", HttpStatus.BAD_GATEWAY),

    // Transaction errors (6300-6399)
    TRANSACTION_NOT_FOUND(6301, "Transaction not found", HttpStatus.NOT_FOUND),
    TRANSACTION_STATUS_INVALID(6302, "Invalid transaction status", HttpStatus.BAD_REQUEST),

    // Validation errors (4000-4099)
    VALIDATION_ERROR(4001, "Validation error", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(4002, "Invalid request", HttpStatus.BAD_REQUEST),

    // System errors (5000-5099)
    INTERNAL_SERVER_ERROR(5001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(5002, "Database error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
} 