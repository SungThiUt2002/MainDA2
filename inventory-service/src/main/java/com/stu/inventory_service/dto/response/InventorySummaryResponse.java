package com.stu.inventory_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryResponse {
    
    private Long totalItems;
    private Long activeItems;
    private Long availableItems;
    private Long outOfStockItems;
    private Long lowStockItems;
    private Long totalStock;
    private Long availableStock;
    private Long reservedStock;
    private Long soldStock;
    private Long pendingAlerts;
    private Long highPriorityAlerts;
} 