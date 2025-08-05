package com.stu.product_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Định nghĩa các mã lỗi nghiệp vụ và hệ thống cho Product Service.
 */
@Getter
public enum ErrorCode {

    UNAUTHENTICATED(6001, "Authentication required", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(6002, "Access denied", HttpStatus.FORBIDDEN),

    CATEGORY_NOT_FOUND(2001, "Category not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(2002, "Product not found", HttpStatus.NOT_FOUND),
    TAG_NOT_FOUND(2003, "Tag not found", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND(2004, "Variant not found", HttpStatus.NOT_FOUND),
    IMAGE_NOT_FOUND(2005, "Image not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS(3001, "Category already exists", HttpStatus.CONFLICT),
    PRODUCT_ALREADY_EXISTS(3002, "Product already exists", HttpStatus.CONFLICT),
    TAG_ALREADY_EXISTS(3003, "Tag already exists", HttpStatus.CONFLICT),
    IMAGE_ALREADY_EXISTS(3004, "Image already exists", HttpStatus.CONFLICT),
    SKU_ALREADY_EXISTS(3002, "Sku already exists", HttpStatus.CONFLICT),
    INVALID_REQUEST(4001, "Invalid request", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(5000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    BRAND_NOT_FOUND(2006, "Brand not found", HttpStatus.NOT_FOUND),
    BRAND_NAME_EXISTS(3005, "Brand name already exists", HttpStatus.CONFLICT),
    VALIDATION_FAILED(4002, "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_INPUT( 5001, "File không được để trống",HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(5002,"Không thể lưu file: ",HttpStatus.INTERNAL_SERVER_ERROR)

    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
} 