package com.stu.common_dto.event.ProductEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateEvent {
    private Long productId;
    private String productName;
    private BigDecimal newPrice;
    private Boolean isActive;
} 