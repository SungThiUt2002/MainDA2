//package com.stu.inventory_service.controller;
//
//import com.stu.inventory_service.dto.request.StockImportRequest;
//import com.stu.inventory_service.dto.response.InventoryItemResponse;
//import com.stu.inventory_service.dto.response.InventoryTransactionResponse;
//import com.stu.inventory_service.entity.InventoryItem;
//import com.stu.inventory_service.mapper.InventoryItemMapper;
//import com.stu.inventory_service.service.InventoryItemService;
//import com.stu.inventory_service.util.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
///**
// * Controller cho API nhập kho.
// * - Xác thực user qua JWT, lấy userId từ token.
// * - Gọi service để xử lý nhập kho, ghi nhận lịch sử, tạo alert nếu cần.
// */
//@RestController
//@RequestMapping("/api/inventory/import")
//@RequiredArgsConstructor
//public class StockImportController {
//
//    private final InventoryItemService inventoryItemService;
//    private final InventoryItemMapper inventoryItemMapper;
//
////    // Hàm nhập kho
////    @PostMapping("/{inventoryItemId}")
////    public ResponseEntity<InventoryItemResponse> importStock(
////        @PathVariable Long inventoryItemId,
////        @RequestBody StockImportRequest request,
////        @RequestHeader("Authorization") String authorizationHeader) {
////
////        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
////        InventoryItem inventoryItem = inventoryItemService.importStock(inventoryItemId, request, token);
////
////        InventoryItemResponse response = inventoryItemMapper.toResponse(inventoryItem);
////        return ResponseEntity.ok(response);
////    }
//}