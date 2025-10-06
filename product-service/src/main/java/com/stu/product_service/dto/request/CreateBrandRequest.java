package com.stu.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class CreateBrandRequest {
    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 255, message = "Tên thương hiệu tối đa 255 ký tự")
    private String name;

    private String description;
    
    private String status = "ACTIVE"; // Default status
} 