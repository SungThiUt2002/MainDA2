package com.stu.shipping_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    SHIPPING_ORDER_NOT_FOUND(7001, "Shipping order not found", HttpStatus.NOT_FOUND),
    SHIPPING_STATUS_INVALID(7002, "Invalid shipping status", HttpStatus.BAD_REQUEST),
    SHIPPING_ALREADY_DELIVERED(7003, "Shipping order already delivered", HttpStatus.CONFLICT),
    SHIPPING_ALREADY_CANCELLED(7004, "Shipping order already cancelled", HttpStatus.CONFLICT),
    VALIDATION_ERROR(4001, "Validation error", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(5001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    SHIPPING_ORDER_EXISTED(5002, "Đơn hàng này đã được vận chuyển trước đây",HttpStatus.CONFLICT)
    ;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
} 