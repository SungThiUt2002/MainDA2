package com.stu.product_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductImageRequest {
    @NotNull(message = "productId không được để trống")
    private Long productId;
//    private Long productVariantId; // null nếu là ảnh chung cho sản phẩm

    private String url;
    private Boolean isThumbnail;
} 