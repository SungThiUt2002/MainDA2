package com.stu.inventory_service.mapper;

import com.stu.inventory_service.dto.request.InventoryTransactionRequest;
import com.stu.inventory_service.dto.response.InventoryTransactionResponse;
import com.stu.inventory_service.entity.InventoryItem;
import com.stu.inventory_service.entity.InventoryTransaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InventoryTransactionMapper {
    
    public InventoryTransaction toEntity(InventoryTransactionRequest request, InventoryItem inventoryItem) {
        if (request == null || inventoryItem == null) {
            return null;
        }
        
        InventoryTransaction entity = new InventoryTransaction();
        entity.setInventoryItem(inventoryItem);
        entity.setTransactionType(request.getTransactionType());
        entity.setQuantity(request.getQuantity());
        entity.setReference(request.getReference());
        entity.setUserId(request.getUserId());
        entity.setNotes(request.getNotes());
        
        return entity;
    }
    
    public InventoryTransactionResponse toResponse(InventoryTransaction entity) {
        if (entity == null) {
            return null;
        }
        
        InventoryTransactionResponse response = new InventoryTransactionResponse();
        response.setId(entity.getId());
        response.setInventoryItemId(entity.getInventoryItem().getId());
        response.setTransactionType(entity.getTransactionType());
        response.setQuantity(entity.getQuantity());
        response.setPreviousQuantity(entity.getPreviousQuantity());
        response.setNewQuantity(entity.getNewQuantity());
        response.setReference(entity.getReference());
        response.setUserId(entity.getUserId());
        response.setNotes(entity.getNotes());
        response.setCreatedAt(entity.getCreatedAt());
        
        return response;
    }
    
    public List<InventoryTransactionResponse> toResponseList(List<InventoryTransaction> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
} 