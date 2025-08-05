package com.stu.cart_service.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import com.stu.cart_service.enums.CartStatus;

/**
 * DTO request cho tạo/sửa giỏ hàng
 */
@Data
public class CartRequest {
    @NotNull(message = "userId không được để trống")
    private Long userId;
    private CartStatus status; // Có thể để null khi tạo mới (mặc định ACTIVE)
} 