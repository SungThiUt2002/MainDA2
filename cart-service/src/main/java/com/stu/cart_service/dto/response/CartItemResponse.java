package com.stu.cart_service.dto.response;

import lombok.Data;
import com.stu.cart_service.enums.CartItemStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItemResponse {
    private Long id;
    private Long productId;;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private CartItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 