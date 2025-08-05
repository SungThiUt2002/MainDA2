package com.stu.inventory_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    
    // Inventory Item Errors (1000-1099)
    INVENTORY_ITEM_NOT_FOUND(1001, "Inventory item not found", HttpStatus.NOT_FOUND),
    INVENTORY_ITEM_ALREADY_EXISTS(1002, "Inventory item already exists", HttpStatus.CONFLICT),
    INSUFFICIENT_STOCK(1003, "Không đủ hàng tồn kho", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(1004, "Invalid quantity value", HttpStatus.BAD_REQUEST),
    SKU_ALREADY_EXISTS(1005, "SKU already exists", HttpStatus.CONFLICT),
    PRODUCT_VARIANT_NOT_FOUND(1006, "Product variant not found", HttpStatus.NOT_FOUND),
    
    // Transaction Errors (1100-1199)
    TRANSACTION_NOT_FOUND(1101, "Transaction not found", HttpStatus.NOT_FOUND),
    INVALID_TRANSACTION_TYPE(1102, "Invalid transaction type", HttpStatus.BAD_REQUEST),
    TRANSACTION_FAILED(1103, "Transaction failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INSUFFICIENT_AVAILABLE_QUANTITY(1104, "Insufficient available quantity", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_RESERVED_QUANTITY(1105, "Insufficient reserved quantity", HttpStatus.BAD_REQUEST),
    
    // Alert Errors (1200-1299)
    ALERT_NOT_FOUND(1201, "Alert not found", HttpStatus.NOT_FOUND),
    ALERT_ALREADY_RESOLVED(1202, "Alert is already resolved", HttpStatus.BAD_REQUEST),
    INVALID_ALERT_TYPE(1203, "Invalid alert type", HttpStatus.BAD_REQUEST),
    
    // Validation Errors (4000-4099)
    VALIDATION_ERROR(4001, "Validation error", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(4002, "Invalid request", HttpStatus.BAD_REQUEST),
    
    // System Errors (5000-5099)
    INTERNAL_SERVER_ERROR(5001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(5002, "Database error", HttpStatus.INTERNAL_SERVER_ERROR),
    KAFKA_ERROR(5003, "Kafka communication error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_ACCESS_ERROR(5004, "Data access error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_INTEGRITY_VIOLATION(5006, "Data integrity constraint violation", HttpStatus.CONFLICT),
    RESOURCE_LOCKED(5007, "Resource is currently locked", HttpStatus.LOCKED),
    
    // Business Logic Errors (6000-6099)
    BUSINESS_RULE_VIOLATION(6001, "Business rule violation", HttpStatus.CONFLICT),
    OPERATION_NOT_ALLOWED(6002, "Operation not allowed", HttpStatus.FORBIDDEN),
    CONCURRENT_MODIFICATION(6003, "Concurrent modification detected", HttpStatus.CONFLICT),
    
    // File & IO Errors (7000-7099)
    FILE_NOT_FOUND(7001, "File not found", HttpStatus.NOT_FOUND),
    FILE_SIZE_EXCEEDED(7002, "File size exceeded maximum limit", HttpStatus.PAYLOAD_TOO_LARGE),
    IO_OPERATION_FAILED(7003, "IO operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // Timeout Errors (8000-8099)
    REQUEST_TIMEOUT(8001, "Request timeout", HttpStatus.GATEWAY_TIMEOUT),
    
    // HTTP & Web Errors (9000-9099)
    HTTP_METHOD_NOT_SUPPORTED(9003, "HTTP method not supported", HttpStatus.METHOD_NOT_ALLOWED),
    MEDIA_TYPE_NOT_SUPPORTED(9004, "Media type not supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INVALID_JSON_FORMAT(9005, "Invalid JSON format or request body", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(9006, "Missing required parameter", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER_TYPE(9007, "Invalid parameter type", HttpStatus.BAD_REQUEST),
    ILLEGAL_ARGUMENT(9008, "Invalid argument", HttpStatus.BAD_REQUEST),
    ILLEGAL_STATE(9009, "Invalid operation state", HttpStatus.CONFLICT),
    UNSUPPORTED_OPERATION(9010, "Operation not supported", HttpStatus.NOT_IMPLEMENTED);
    
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
} 