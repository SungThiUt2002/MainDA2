package com.stu.product_service.dto.request;

import lombok.Data;

@Data
public class UpdateBrandRequest {
    private String name;
    private String description;
    private Boolean isActive;
} 