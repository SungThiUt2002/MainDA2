package com.stu.inventory_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.stu.inventory_service.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {
    
    private Long id;
    private Long inventoryItemId;
    private TransactionType transactionType;
    private Integer quantity;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String reference;
    private Long userId;
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
} 