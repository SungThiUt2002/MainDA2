package com.stu.product_service.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;



@Data
public class UpdateCategoryRequest {
    @Size(max = 255, message = "Tên danh mục tối đa 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;
}
