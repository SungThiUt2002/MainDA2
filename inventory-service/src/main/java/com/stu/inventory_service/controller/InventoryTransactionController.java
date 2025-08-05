package com.stu.inventory_service.controller;

import com.stu.inventory_service.dto.request.InventoryTransactionRequest;
import com.stu.inventory_service.dto.response.InventoryTransactionResponse;
import com.stu.inventory_service.service.InventoryTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionController {
    
    private final InventoryTransactionService transactionService;
    
    @GetMapping("/{id}")
    public ResponseEntity<InventoryTransactionResponse> getTransactionById(@PathVariable Long id) {
        log.info("Getting transaction by ID: {}", id);
        InventoryTransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/inventory-item/{inventoryItemId}")
    public ResponseEntity<List<InventoryTransactionResponse>> getTransactionsByInventoryItemId(@PathVariable Long inventoryItemId) {
        log.info("Getting transactions for inventory item ID: {}", inventoryItemId);
        List<InventoryTransactionResponse> responses = transactionService.getTransactionsByInventoryItemId(inventoryItemId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/inventory-item/{inventoryItemId}/paginated")
    public ResponseEntity<Page<InventoryTransactionResponse>> getTransactionsByInventoryItemIdPaginated(
            @PathVariable Long inventoryItemId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting paginated transactions for inventory item ID: {}", inventoryItemId);
        Page<InventoryTransactionResponse> responses = transactionService.getTransactionsByInventoryItemId(inventoryItemId, pageable);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/reference/{reference}")
    public ResponseEntity<List<InventoryTransactionResponse>> getTransactionsByReference(@PathVariable String reference) {
        log.info("Getting transactions by reference: {}", reference);
        List<InventoryTransactionResponse> responses = transactionService.getTransactionsByReference(reference);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<InventoryTransactionResponse>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting transactions between {} and {}", startDate, endDate);
        List<InventoryTransactionResponse> responses = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping
    public ResponseEntity<Page<InventoryTransactionResponse>> getAllTransactions(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting all transactions with pagination");
        Page<InventoryTransactionResponse> responses = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(responses);
    }
} 