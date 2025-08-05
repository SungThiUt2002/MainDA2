package com.stu.product_service.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
//    private Integer stock; // số lượng tồn kho
//    private Integer soldQuantity;// số lượng sản phẩm đã bán
    private CategoryResponse category;
    private BrandResponse brand;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
    private List<ProductImageResponse> images;
}