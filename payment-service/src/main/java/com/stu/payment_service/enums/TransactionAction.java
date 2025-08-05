package com.stu.payment_service.enums;

/**
 * Các hành động giao dịch thanh toán
 */
public enum TransactionAction {
    /** Khởi tạo giao dịch */
    INIT,
    /** Thanh toán */
    PAY,
    /** Hoàn tiền */
    REFUND,
    /** Hủy giao dịch */
    CANCEL
} 