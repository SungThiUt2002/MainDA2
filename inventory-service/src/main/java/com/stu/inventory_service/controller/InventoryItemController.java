package com.stu.inventory_service.controller;


import com.stu.inventory_service.dto.request.StockImportRequest;
import com.stu.inventory_service.dto.response.ApiResponse;
import com.stu.inventory_service.dto.response.InventoryItemResponse;
import com.stu.inventory_service.entity.InventoryItem;
import com.stu.inventory_service.mapper.InventoryItemMapper;
import com.stu.inventory_service.service.InventoryItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@Slf4j
@RestController
@RequestMapping("/api/v1/inventory-items")
@RequiredArgsConstructor
public class InventoryItemController {
    
    private final InventoryItemService inventoryItemService;
    private final InventoryItemMapper inventoryItemMapper;

    // 1.Lấy số lượng tồn kho theo productid
    @GetMapping("/products/{productId}/available-quantity")
    public ResponseEntity<Integer> getAvailableQuantity(@PathVariable Long productId) {
        var item = inventoryItemService.getAvailableQuantity(productId);
        return ResponseEntity.ok(item);
    }

    // 2.Lấy số lượng sản phẩm đã bán theo productId
    @GetMapping("/products/{productId}/sold-quantity")
    public  ResponseEntity<Integer> getSoldQuantity(@PathVariable Long productId){
        var item = inventoryItemService.getSoldQuantity(productId);
        return ResponseEntity.ok(item);
    }

    // 3. Hàm nhập kho
    @PostMapping("/{productId}")
    public ResponseEntity<InventoryItemResponse> importStock(
            @PathVariable Long productId,
            @RequestBody StockImportRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        InventoryItem inventoryItem = inventoryItemService.importStock(productId, request, token);

        InventoryItemResponse response = inventoryItemMapper.toResponse(inventoryItem);
        return ResponseEntity.ok(response);
    }

    // 4. lấy thông tin theo productId
    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryItemResponse> getInventoryItemByProductVariantId(@PathVariable Long productId) {
        log.info("Getting inventory item by product ID: {}", productId);
        InventoryItem inventoryItem = inventoryItemService.getInventoryItemByProductId(productId);
        InventoryItemResponse response = inventoryItemMapper.toResponse(inventoryItem);
        return ResponseEntity.ok(response);
    }

    // Endpoint chính - lấy dữ liệu thực từ database
    @GetMapping("/all")
    public ResponseEntity<List<InventoryItemResponse>> getAllActiveInventoryItems() {
        log.info("Getting all active inventory items from database");
        try {
            List<InventoryItemResponse> responses = inventoryItemService.getAllActiveInventoryItems();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting inventory items from database: ", e);
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<InventoryItemResponse>> getAvailableInventoryItems() {
        log.info("Getting available inventory items");
        List<InventoryItemResponse> responses = inventoryItemService.getAvailableInventoryItems();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItemResponse>> getLowStockItems() {
        log.info("Getting low stock items");
        List<InventoryItemResponse> responses = inventoryItemService.getLowStockItems();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/needing-reorder")
    public ResponseEntity<List<InventoryItemResponse>> getItemsNeedingReorder() {
        log.info("Getting items needing reorder");
        List<InventoryItemResponse> responses = inventoryItemService.getItemsNeedingReorder();
        return ResponseEntity.ok(responses);
    }

} 