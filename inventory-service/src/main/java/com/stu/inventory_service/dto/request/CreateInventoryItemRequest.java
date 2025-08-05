package com.stu.inventory_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryItemRequest {
    
    @NotNull(message = "Product variant ID is required")
    @Min(value = 1, message = "Product variant ID must be greater than 0")
    private Long productId;
    private String productName;
} 