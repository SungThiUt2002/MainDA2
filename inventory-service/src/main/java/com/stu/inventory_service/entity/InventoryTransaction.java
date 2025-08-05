package com.stu.inventory_service.entity;

import com.stu.inventory_service.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;//
    
    @Column(name = "quantity")
    private Integer quantity;//Số lượng thay đổi
    
    @Column(name = "previous_quantity")
    private Integer previousQuantity;//Số lượng trước khi thay đổi
    
    @Column(name = "new_quantity")
    private Integer newQuantity;//Số lượng sau khi thay đổi
    
    @Column(name = "reference", length = 255)
    private String reference;//Tham chiếu (order_id, etc.)
    
    @Column(name = "user_id")
    private Long userId;//ID người thực hiện
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
//    // Constructor for convenience
//    public InventoryTransaction(InventoryItem inventoryItem, TransactionType transactionType,
//                              Integer quantity, Integer previousQuantity, Integer newQuantity,
//                              String reference, Long userId, String notes) {
//        this.inventoryItem = inventoryItem;
//        this.transactionType = transactionType;
//        this.quantity = quantity;
//        this.previousQuantity = previousQuantity;
//        this.newQuantity = newQuantity;
//        this.reference = reference;
//        this.userId = userId;
//        this.notes = notes;
//    }
} 