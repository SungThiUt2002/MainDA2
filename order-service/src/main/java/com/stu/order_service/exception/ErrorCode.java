package com.stu.order_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // System errors
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_ACCESS_ERROR(9002, "Data access error", HttpStatus.INTERNAL_SERVER_ERROR),

    // Order errors
    ORDER_NOT_FOUND(1001, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_ITEM_NOT_FOUND(1002, "Order item not found", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_CONFIRM(1003, "Order đã được xác nhận", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_REQUEST(1004, "Invalid order request", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK(1005, "Product is out of stock", HttpStatus.BAD_REQUEST),
    DUPLICATE_ORDER(1006, "Đơn hàng đã được đặt rồi", HttpStatus.CONFLICT),
    PERMISSION_DENIED(1007, "Quyền bị từ chối, bạn không phải là người sở hữu tài khoản này", HttpStatus.FORBIDDEN),
    VALIDATION_ERROR(1008, "Validation failed", HttpStatus.BAD_REQUEST),
    CONCURRENT_MODIFICATION(1009, "Concurrent modification detected, please retry", HttpStatus.CONFLICT),

    ORDER_INFO_INCOMPLETE(7001, "Chưa đủ thông tin để tạo đơn hàng", HttpStatus.BAD_REQUEST),
    ORDER_STATUS_INVALID(7002, "Chỉ đơn hàng chờ xác nhận mới được xác nhận", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_DELIVERED(7003, "Đơn hàng đã giao thành công", HttpStatus.CONFLICT),
    ORDER_ALREADY_CANCELLED(7004, " Đơn hàng đã bị hủy", HttpStatus.CONFLICT),
    ORDER_ALREADY_RETURN(7004, "Đơn hàng đã hoàn hàng", HttpStatus.CONFLICT),
    ORDER_ALREADY_FINALIZED(7007, "Đơn hàng đã kết thúc", HttpStatus.CONFLICT),
    INVALID_ORDER_STATUS_TRANSITION(7008,"Chuyển đổi trạng thái không hợp lệ", HttpStatus.BAD_REQUEST),

    ORDER_CANNOT_BE_CANCELLED(7005, "Đơn đã bàn giao cho bên vận chuyển hoặc hoàn tất, không thể hủy",HttpStatus.BAD_REQUEST),

    ORDER_NOT_CONFIRM(7006,"Đơn hàng chưa được xác nhận", HttpStatus.BAD_REQUEST)
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