package com.stu.inventory_service.repository;

import com.stu.inventory_service.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findItemByProductId(Long productId);
//    Optional<InventoryItem> findFirstByProductId(Long productId);

    void deleteByProductId(Long productId);

    // Tìm tất cả items đang active
    List<InventoryItem> findByIsActiveTrue();

    // Tìm tất cả items có sẵn để bán
    List<InventoryItem> findByIsAvailableTrueAndIsActiveTrue();

    // Tìm items hết hàng (available quantity == 0)
    List<InventoryItem> findByAvailableQuantityAndIsActiveTrue(Integer availableQuantity);

    // Tìm items sắp hết hàng (available <= low_stock_threshold)
    @Query("SELECT i FROM InventoryItem i WHERE i.availableQuantity <= i.lowStockThreshold AND i.availableQuantity > 0 AND i.isActive = true")
    List<InventoryItem> findLowStockItems();

    // Tìm items cần đặt hàng lại (available <= reorder_point)
    @Query("SELECT i FROM InventoryItem i WHERE i.availableQuantity <= i.reorderPoint AND i.isActive = true")
    List<InventoryItem> findItemsNeedingReorder();

}
