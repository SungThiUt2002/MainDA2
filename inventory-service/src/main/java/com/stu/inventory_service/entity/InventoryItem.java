package com.stu.inventory_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;//ID tham chiếu đến product-service (một sản phẩm có nhiều biến thể, mỗi biến thể có 1 inventory item)
    private String productName;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0; //Tổng số lượng đã nhập vào kho (cả sản phẩm đã bán + hiện có để bán)

    @Column(name = "locked_quantity", nullable = false)
    private Integer lockedQuantity = 0; //Người dùng đặt hàng (chưa thanh toán)

    @Column(name = "sold_quantity", nullable = false)
    private Integer soldQuantity = 0; //Số lượng đã bán thành công
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;//Số lượng có sẵn để bán
    
    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold = 10;//Ngưỡng cảnh báo hết hàng
    
    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint = 5;//Điểm đặt hàng lại
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;//Có bán không (ví dụ Tạm ngưng bán, hết hàng, kiểm kê, thu hồi)
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;//Có hoạt động không(Xóa mềm, ngưng kinh doanh, giữ lịch sử)

    @Column(name = "last_sale_date")
    private LocalDateTime lastSaleDate; // Lần bán cuối cùng
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryTransaction> transactions = new ArrayList<>();
    
//    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<InventoryAlert> alerts = new ArrayList<>();
    
    // Business methods
    public void updateAvailableQuantity() {
        this.availableQuantity = this.totalQuantity - this.lockedQuantity - this.soldQuantity; // sản phẩm có sẵn để bán = tổng sản phẩm - sản phẩm đã bán
    }
    
    public boolean isLowStock() {
        return this.availableQuantity <= this.lowStockThreshold && this.availableQuantity > 0; // ==? đưa ra cảnh báo
    }
    
    public boolean isOutOfStock() {
        return this.availableQuantity == 0;
    } // Cảnh báo hết hàng
    
    public boolean needsReorder() {
        return this.availableQuantity <= this.reorderPoint;
    }
} 