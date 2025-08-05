package com.stu.order_service.enums;

public enum OrderStatus {
    CREATED, // Tạo mớiddodwn hàng
    CONFIRM_INFORMATION, // xác nahnaj đầy đủ thông tin cho đơn hàng khi đặt hàng
    CONFIRMED,// Đã thanh toán online , admin xác nhận → Xác nhận đơn hàn
    PENDING_CONFIRMATION,//COD: chờ Admin xác nhận thủ công
    PENDING_PAYMENT, // Chờ thanh toan
    DELIVERING, // Đang giao hàng
    DELIVERY_SUCCESSFUL, // Giao hàng thành công
    CANCELLED, // bị hủy (đối với đơn chưa giao hàng)
    RETURNED,// Hoàn hàng (đối với đơn đã giao hang)
    PAYMENT_FAILED // thanh toán thất bại
} 