package com.stu.product_service.exception;

/**
 * Custom exception cho các lỗi nghiệp vụ hoặc hệ thống trong Product Service.
 */
public class ProductServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final ErrorCode errorCode;
    private final Object[] messageArgs;

    public ProductServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.messageArgs = null;
    }

    public ProductServiceException(ErrorCode errorCode, Object... messageArgs) {
        super(formatMessage(errorCode.getMessage(), messageArgs));
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }

    public ProductServiceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.messageArgs = null;
    }

    public ProductServiceException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.messageArgs = null;
    }

    public ProductServiceException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.messageArgs = null;
    }

    private static String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return String.format(message, args);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getMessageArgs() {
        return messageArgs;
    }
} 