package com.stu.inventory_service.dto.request;

import lombok.Data;

// Hàm nhập kho
@Data
public class StockImportRequest {
    private Integer quantity;
    private String reason;
} 