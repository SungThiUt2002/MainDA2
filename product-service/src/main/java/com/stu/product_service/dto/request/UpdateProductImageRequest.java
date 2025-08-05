package com.stu.product_service.dto.request;

import lombok.Data;

@Data
public class UpdateProductImageRequest {
    private String url;
    private Boolean isThumbnail;
} 