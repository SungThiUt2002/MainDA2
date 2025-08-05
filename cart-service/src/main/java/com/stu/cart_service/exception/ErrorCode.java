package com.stu.cart_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    UNAUTHENTICATED(3001, "Authentication required", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(3002, "Bạn không phải người sở hữu giỏ hàng này, bạn không có quyền thêm sản phẩm vào giỏ hàng", HttpStatus.FORBIDDEN),
    // Cart errors (1000-1099)
    CART_NOT_FOUND(1001, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ALREADY_EXISTS(1002, "Cart already exists", HttpStatus.CONFLICT),
    CART_STATUS_INVALID(1003, "Invalid cart status", HttpStatus.BAD_REQUEST),
    CART_NOT_ACTIVE(1004, "Cart is not active", HttpStatus.BAD_REQUEST),

    // CartItem errors (1100-1199)
    CART_ITEM_NOT_FOUND(1101, "Cart item not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_ACTIVE(1106,"Sản phẩm hiện không tồn tại, vui lòng thêm sảm phẩm khác",HttpStatus.BAD_REQUEST),
    CART_ITEM_ALREADY_EXISTS(1102, "Cart item already exists", HttpStatus.CONFLICT),
    CART_ITEM_STATUS_INVALID(1103, "Invalid cart item status", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK(1104, "Product is out of stock", HttpStatus.BAD_REQUEST),
    PRICE_CHANGED(1105, "Product price has changed", HttpStatus.CONFLICT),

    // Product errors (2000-2099)
    PRODUCT_NOT_FOUND(2001, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_AVAILABLE(2002, "Product is not available", HttpStatus.BAD_REQUEST),
    PRODUCT_VARIANT_NOT_FOUND(2003, "Product variant not found", HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_NOT_AVAILABLE(2004, "Product variant is not available", HttpStatus.BAD_REQUEST),
    PRODUCT_SYNC_ERROR(2005, "Error synchronizing product information", HttpStatus.INTERNAL_SERVER_ERROR),

    // Validation errors (4000-4099)
    VALIDATION_ERROR(4001, "Validation error", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(4002, "Invalid request", HttpStatus.BAD_REQUEST),

    // System errors (5000-5099)
    INTERNAL_SERVER_ERROR(5001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(5002, "Database error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
} 