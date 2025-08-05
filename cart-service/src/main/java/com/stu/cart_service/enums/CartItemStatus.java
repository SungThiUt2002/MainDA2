package com.stu.cart_service.enums;

/**
 * Enum đại diện cho trạng thái của CartItem (sản phẩm trong giỏ hàng)
 */
public enum CartItemStatus {
    ACTIVE,         // Đang trong giỏ
    REMOVED,        // Cửa hàng không bán nữa
    OUT_OF_STOCK,   // Hết hàng
    CHECKED_OUT,    // Đã đặt hàng thành công
    EXPIRED         // Hết hạn giữ chỗ
}

/*Nếu hệ thống đơn giản, chỉ cần ACTIVE và REMOVED.
Nếu muốn nâng cao trải nghiệm user (cảnh báo hết hàng, thay đổi giá...), nên bổ sung các trạng thái OUT_OF_STOCK, PRICE_CHANGED.
Nếu có nghiệp vụ giữ hàng tạm thời, nên có EXPIRED.
 */