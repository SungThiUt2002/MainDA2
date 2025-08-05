package com.stu.cart_service.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * DTO cho thao tác cập nhật sản phẩm (cập nhập giá theo product service)
 */
@Data
public class UpdateCartItemRequest {
    private Long productId;
    private String productName;
    private BigDecimal price;
} 