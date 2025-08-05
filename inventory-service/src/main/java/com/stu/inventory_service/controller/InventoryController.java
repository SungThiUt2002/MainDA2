package com.stu.inventory_service.controller;

import com.stu.inventory_service.dto.response.InventoryItemResponse;
import com.stu.inventory_service.service.InventoryItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller quản lý các API endpoints liên quan đến inventory
 * Cung cấp các chức năng CRUD, stock management và reporting
 * Quản lý các API tổng hợp, báo cáo, nghiệp vụ kho tổng thể.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryItemService inventoryItemService;

    @GetMapping("/items/{inventoryItemId}/available")
    public ResponseEntity<Map<String, Object>> checkStockAvailability(
            @PathVariable Long inventoryItemId,
            @RequestParam Integer quantity) {
        boolean isAvailable = inventoryItemService.isStockAvailable(inventoryItemId, quantity);
        Integer availableQuantity = inventoryItemService.getAvailableQuantity(inventoryItemId);

        return ResponseEntity.ok(Map.of(
            "inventoryItemId", inventoryItemId,
            "requestedQuantity", quantity,
            "availableQuantity", availableQuantity,
            "isAvailable", isAvailable
        ));
    }


    // ==================== REPORTS ====================
    /**
     * Lấy danh sách items có stock thấp (low stock)
     *
     * @return ResponseEntity<List<InventoryItemResponse>> danh sách low stock items
     */
    @GetMapping("/reports/low-stock")
    public ResponseEntity<List<InventoryItemResponse>> getLowStockItems() {
        List<InventoryItemResponse> items = inventoryItemService.getLowStockItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Lấy danh sách items hết hàng (out of stock)
     *
     * @return ResponseEntity<List<InventoryItemResponse>> danh sách out of stock items
     */
    @GetMapping("/reports/out-of-stock")
    public ResponseEntity<List<InventoryItemResponse>> getOutOfStockItems() {
        // TODO: Implement getOutOfStockItems method in service
        return ResponseEntity.ok(List.of());
    }

    /**
     * Lấy danh sách items có stock quá cao (overstock)
     *
     * @return ResponseEntity<List<InventoryItemResponse>> danh sách overstock items
     */
    @GetMapping("/reports/overstock")
    public ResponseEntity<List<InventoryItemResponse>> getOverstockItems() {
        // TODO: Implement getOverstockItems method in service
        return ResponseEntity.ok(List.of());
    }

    // ==================== HEALTH CHECK ====================
    /**
     * Health check endpoint
     * Kiểm tra trạng thái hoạt động của service
     *
     * @return ResponseEntity<Map<String, Object>> thông tin trạng thái service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "inventory-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}