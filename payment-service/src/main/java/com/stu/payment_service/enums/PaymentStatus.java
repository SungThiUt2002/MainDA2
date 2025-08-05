package com.stu.payment_service.enums;

/**
 * Trạng thái của thanh toán
 */
public enum PaymentStatus {
    PENDING,// Đang chờ thanh toán
    /** Thanh toán thành công */
    SUCCESS,
    /** Thanh toán thất bại */
    FAILED,
    /** Đã hoàn tiền */
    REFUNDED,
    /** Đã hủy */
    CANCELLED
} 