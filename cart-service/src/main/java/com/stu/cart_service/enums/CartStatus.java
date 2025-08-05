package com.stu.cart_service.enums;

/**
 * Enum đại diện cho trạng thái của giỏ hàng (Cart)
 */
public enum CartStatus {
    ACTIVE,         // Đang hoạt động
    CHECKED_OUT,    // Đã thanh toán
    INACTIVE,      // chưa xóa, nhưng không hặt đông --> ví dụ bị vô hiệu hóa
    MERGED,         // Đã gộp vào cart khác
    DELETED,        // Đã xóa
    LOCKED          // Đang bị khóa tạm thời
}