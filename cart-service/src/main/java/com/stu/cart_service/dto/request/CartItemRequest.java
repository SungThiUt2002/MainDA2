package com.stu.cart_service.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import com.stu.cart_service.enums.CartItemStatus;
import java.math.BigDecimal;

/**
 * DTO request cho thêm/sửa sản phẩm trong giỏ hàng
 */
@Data
public class CartItemRequest {
    @NotNull(message = "productVariantId không được để trống")
    private Long productId;

    private String productName;

    @NotNull(message = "quantity không được để trống")
    @Min(value = 1, message = "Số lượng phải >= 1")
    private Integer quantity;

    private BigDecimal price;

    private CartItemStatus status; // Có thể để null khi tạo mới (mặc định ACTIVE)
} 