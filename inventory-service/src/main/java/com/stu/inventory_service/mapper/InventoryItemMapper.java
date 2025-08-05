package com.stu.inventory_service.mapper;

import com.stu.inventory_service.dto.request.CreateInventoryItemRequest;
import com.stu.inventory_service.dto.request.UpdateProductNameRequest;
import com.stu.inventory_service.dto.response.InventoryItemResponse;
import com.stu.inventory_service.entity.InventoryItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InventoryItemMapper {
    
    public InventoryItem toEntity(CreateInventoryItemRequest request) {
        if (request == null) {
            return null;
        }
        
        InventoryItem entity = new InventoryItem();
        entity.setProductId(request.getProductId());
        entity.setProductName(request.getProductName());
        
        // Cập nhật available quantity
        entity.updateAvailableQuantity();
        
        return entity;
    }
    
    public InventoryItemResponse toResponse(InventoryItem entity) {
        if (entity == null) {
            return null;
        }
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(entity.getId());
        response.setProductId(entity.getProductId());
        response.setProductName(entity.getProductName());
        response.setTotalQuantity(entity.getTotalQuantity());
        response.setSoldQuantity(entity.getSoldQuantity());
        response.setLockedQuantity(entity.getLockedQuantity());
        response.setAvailableQuantity(entity.getAvailableQuantity());
        response.setLowStockThreshold(entity.getLowStockThreshold());
        response.setReorderPoint(entity.getReorderPoint());
        response.setIsAvailable(entity.getIsAvailable());
        response.setIsActive(entity.getIsActive());
        response.setIsLowStock(entity.isLowStock());
        response.setIsOutOfStock(entity.isOutOfStock());
        response.setNeedsReorder(entity.needsReorder());
        response.setLastSaleDate(entity.getLastSaleDate());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        
        return response;
    }
    
    public List<InventoryItemResponse> toResponseList(List<InventoryItem> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public void updateEntityFromRequest(InventoryItem entity, UpdateProductNameRequest request) {
        if (entity == null || request == null) {
            return;
        }
        
        if (request.getProductName() != null) {
            entity.setProductName(request.getProductName());
        }
    }
} 