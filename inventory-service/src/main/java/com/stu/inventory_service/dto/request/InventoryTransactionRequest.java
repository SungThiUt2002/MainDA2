package com.stu.inventory_service.dto.request;


import com.stu.inventory_service.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionRequest {
    
    @NotNull(message = "Inventory item ID is required")
    @Min(value = 1, message = "Inventory item ID must be greater than 0")
    private Long inventoryItemId;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Min(value = 1, message = "Quantity must be greater than 0")
    @Max(value = 999999, message = "Quantity cannot exceed 999,999")
    private Integer quantity;
    
    @Size(max = 255, message = "Reference cannot exceed 255 characters")
    private String reference;
    
    @Min(value = 1, message = "User ID must be greater than 0")
    private Long userId;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
} 