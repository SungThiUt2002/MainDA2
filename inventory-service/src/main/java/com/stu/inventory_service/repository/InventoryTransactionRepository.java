package com.stu.inventory_service.repository;

import com.stu.inventory_service.entity.InventoryTransaction;

import com.stu.inventory_service.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    
    // Tìm tất cả transactions của một inventory item
    List<InventoryTransaction> findByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId);
    
    // Tìm transactions theo loại
    List<InventoryTransaction> findByTransactionType(TransactionType transactionType);
    
    // Tìm transactions của một inventory item theo loại
    List<InventoryTransaction> findByInventoryItemIdAndTransactionTypeOrderByCreatedAtDesc(
            Long inventoryItemId, TransactionType transactionType);
    
    // Tìm transactions theo reference (order_id, etc.)
    List<InventoryTransaction> findByReference(String reference);
    
    // Tìm transactions theo user_id
    List<InventoryTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Tìm transactions trong khoảng thời gian
    List<InventoryTransaction> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    // Tìm transactions của inventory item trong khoảng thời gian
    List<InventoryTransaction> findByInventoryItemIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long inventoryItemId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Tìm transactions theo loại và khoảng thời gian
    List<InventoryTransaction> findByTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            TransactionType transactionType, LocalDateTime startDate, LocalDateTime endDate);
    
    // Phân trang transactions
    Page<InventoryTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Phân trang transactions của inventory item
    Page<InventoryTransaction> findByInventoryItemIdOrderByCreatedAtDesc(
            Long inventoryItemId, Pageable pageable);
    
    // Đếm transactions theo loại
    long countByTransactionType(TransactionType transactionType);
    
    // Đếm transactions của inventory item
    long countByInventoryItemId(Long inventoryItemId);
    
    // Tìm transactions có quantity lớn hơn một giá trị
    List<InventoryTransaction> findByQuantityGreaterThan(Integer quantity);
    
    // Tìm transactions theo nhiều loại
    List<InventoryTransaction> findByTransactionTypeIn(List<TransactionType> transactionTypes);
    
    // Tìm transactions theo reference pattern (LIKE)
    @Query("SELECT t FROM InventoryTransaction t WHERE t.reference LIKE %:pattern% ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findByReferencePattern(@Param("pattern") String pattern);
    
    // Tìm transactions có notes chứa từ khóa
    @Query("SELECT t FROM InventoryTransaction t WHERE t.notes LIKE %:keyword% ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findByNotesContaining(@Param("keyword") String keyword);
    
    // Tìm transactions theo inventory item và user
    List<InventoryTransaction> findByInventoryItemIdAndUserIdOrderByCreatedAtDesc(
            Long inventoryItemId, Long userId);
    
    // Tìm transactions theo reference và loại
    List<InventoryTransaction> findByReferenceAndTransactionTypeOrderByCreatedAtDesc(
            String reference, TransactionType transactionType);
} 