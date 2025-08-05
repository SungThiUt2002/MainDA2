package com.stu.inventory_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private Integer totalQuantity; // tổng số sản phẩm đã nhập kho
    private Integer lockedQuantity ; //Người dùng đặt hàng (chưa thanh toán)
    private Integer soldQuantity; // số lượng sản phẩm đã bán
    private Integer availableQuantity;// tổng số sản phẩm có sẵn trong kho để bán
    private Integer lowStockThreshold; // ngưỡng cảnh báo hết hàng
    private Integer reorderPoint; // điểm đặt hàng lại
    private Boolean isAvailable; // trạng thái có bán hay khong
    private Boolean isActive; // sản phẩm có hoạt động khô ( ví dụ sản phẩm bị xóa --> false)
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private Boolean needsReorder;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSaleDate; // Lần bán hàng gần nhất --> dùng cho thống kê
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
} 