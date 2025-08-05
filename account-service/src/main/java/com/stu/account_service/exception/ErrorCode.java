package com.stu.account_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    HASH_TOKEN_FAILED(1000,"Failed to hash token", HttpStatus.INTERNAL_SERVER_ERROR),
    // System errors (9xxx)
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9001, "Invalid request key", HttpStatus.BAD_REQUEST),
    DATA_ACCESS_ERROR(9002, "Data access error", HttpStatus.INTERNAL_SERVER_ERROR),

    // User errors (1xxx)
    USER_ALREADY_EXISTS(1001, "User already exists", HttpStatus.CONFLICT),
    USER_NOT_EXISTED(1002, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1003, "Username must be at least 4 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Mật khẩu tối thiểu 8 ký tự, ít nhất 01 chữ in hoa, 01 chữ in thường, 01 chữ số, và 01 ký tự đặc biệt", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1005, "Invalid email format", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(1006, "Email already exists", HttpStatus.CONFLICT),
    INVALID_DOB(1007, "Age must be at least 18", HttpStatus.BAD_REQUEST),

    // Authentication errors (2xxx)
    UNAUTHENTICATED(2001, "Authentication required", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2002, "Access denied - insufficient permissions", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(2003, "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(2004, "Token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(2005, "Invalid refresh token", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_EXPIRED(2006, "Refresh token expired", HttpStatus.BAD_REQUEST),
    TOO_MANY_REQUESTS(2007, "Too many login attempts, please try again later", HttpStatus.TOO_MANY_REQUESTS),
    LOGOUT_FAILED(2008, "Logout failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_LOCKED(2009, "Account is locked", HttpStatus.LOCKED),
    ACCOUNT_DISABLED(2010, "Account is disabled", HttpStatus.UNAUTHORIZED),
    ACCOUNT_EXPIRED(2011, "Account has expired", HttpStatus.UNAUTHORIZED),
    PASSWORD_MISMATCH(2012, "Password does not match", HttpStatus.BAD_REQUEST),
    TOO_MANY_SESSIONS(2013, "Quá nhiều session đang hoạt động", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(401, "Invalid username or password", HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED(429, "Rate limit exceeded", HttpStatus.BAD_REQUEST),

    // Role errors (3xxx)
    ROLE_NOT_FOUND(3001, "Role not found", HttpStatus.NOT_FOUND),
    ROLE_EXISTED(3002, "Role already exists", HttpStatus.CONFLICT),
    INVALID_ROLE_NAME(3003, "Invalid role name format", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_DEFAULT_ROLE(3004, "Cannot delete default roles", HttpStatus.FORBIDDEN),
    CANNOT_MODIFY_DEFAULT_ROLE(3005, "Cannot modify default roles", HttpStatus.FORBIDDEN),

    // Permission errors (4xxx)
    PERMISSION_NOT_FOUND(4001, "Permission not found", HttpStatus.NOT_FOUND),
    PERMISSION_EXISTED(4002, "Permission already exists", HttpStatus.CONFLICT),
    INVALID_PERMISSION_NAME(4003, "Invalid permission name format", HttpStatus.BAD_REQUEST),
    CANNOT_MODIFY_DEFAULT_PERMISSION(4004, "Cannot modify default permissions", HttpStatus.FORBIDDEN),

    // Account management errors (5xxx)
    ACCOUNT_NOT_DELETED(5001, "Cannot delete this account", HttpStatus.FORBIDDEN),
    CANNOT_DELETE_ADMIN(5002, "Cannot delete admin account", HttpStatus.FORBIDDEN),
    CANNOT_DELETE_SELF(5003, "Cannot delete your own account", HttpStatus.FORBIDDEN),
    ACCOUNT_ALREADY_DELETED(5004, "Account already scheduled for deletion", HttpStatus.BAD_REQUEST),
    ACCOUNT_DELETION_EXPIRED(5005, "Account recovery period expired", HttpStatus.BAD_REQUEST),
    DATA_CONFLICT(5006, "Conflict with existing data", HttpStatus.CONFLICT),

    // Business logic errors (6xxx)
    CONCURRENT_MODIFICATION(6001, "Concurrent modification detected, please retry", HttpStatus.CONFLICT),
    VALIDATION_ERROR(6002, "Validation failed", HttpStatus.BAD_REQUEST),

    // File errors (7xxx)
    FILE_NOT_FOUND(7001, "File not found", HttpStatus.NOT_FOUND),
    FILE_TOO_LARGE(7002, "File size exceeds the limit", HttpStatus.PAYLOAD_TOO_LARGE),
    IO_ERROR(7003, "I/O operation failed", HttpStatus.INTERNAL_SERVER_ERROR),

    ADDRESS_NOT_FOUND(10001, "Address not found", HttpStatus.NOT_FOUND),
    INVALID_ADDRESS(10002, "Invalid address", HttpStatus.BAD_REQUEST),
//    DATA_ACCESS_ERROR(9002, "Data access error", HttpStatus.INTERNAL_SERVER_ERROR),
//    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR);

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