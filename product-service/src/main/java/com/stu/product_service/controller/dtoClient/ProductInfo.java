package com.stu.product_service.controller.dtoClient;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductInfo {
    private Long productId;
    private String productName;
    private BigDecimal price;
}
