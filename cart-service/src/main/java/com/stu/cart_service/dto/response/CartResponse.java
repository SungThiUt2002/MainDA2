package com.stu.cart_service.dto.response;

import lombok.Data;
import com.stu.cart_service.enums.CartStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO response cho giỏ hàng
 */
@Data
public class CartResponse {
    private Long id;
    private Long userId;
    private CartStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItemResponse> items;
} 