package com.stu.product_service.dto.response;

import lombok.Data;

@Data
public class BrandResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
} 