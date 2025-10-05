package com.stu.product_service.dto.response;

import lombok.Data;

@Data
public class BrandResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    
    // Getter for status field that frontend expects
    public String getStatus() {
        return isActive != null && isActive ? "ACTIVE" : "INACTIVE";
    }
} 