package com.stu.product_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;


@Data
public class UpdateProductRequest {
    private String name;
    private String description;
    @DecimalMin(value = "0.01", message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;
    private Long categoryId;
    private Long brandId;
    private Boolean isActive;
} 