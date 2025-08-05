package com.stu.product_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductImageResponse {
    private Long id;
    private Long productId;
//    private Long productVariantId; // null nếu là ảnh chung cho sản phẩm
    private String url;
    private Boolean isThumbnail;
} 