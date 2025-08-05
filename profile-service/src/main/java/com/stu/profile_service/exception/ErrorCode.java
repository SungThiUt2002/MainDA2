package com.stu.profile_service.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    ADDRESS_NOT_FOUND(10001, "Address not found", HttpStatus.NOT_FOUND),
    INVALID_ADDRESS(10002, "Invalid address", HttpStatus.BAD_REQUEST),
    DATA_ACCESS_ERROR(9002, "Data access error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
