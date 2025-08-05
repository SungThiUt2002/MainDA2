package com.stu.cart_service.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * DTO cho thao tác thêm sản phẩm vào giỏ hàng
 */
@Data
public class AddCartItemRequest {

    @NotNull(message = "productId không được để trống")
    private Long productId;
    private String productName;

    @NotNull(message = "quantity không được để trống")
    @Min(value = 1, message = "Số lượng phải >= 1")
    private Integer quantity;

    private BigDecimal price;
} 