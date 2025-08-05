package com.stu.cart_service.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductInfo {
    private Long productId;
    private String productName;
    private BigDecimal price;
}
