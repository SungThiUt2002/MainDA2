package com.stu.order_service.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemRequest {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal productPrice;
} 