package com.stu.inventory_service.service;

import com.stu.inventory_service.dto.request.InventoryTransactionRequest;
import com.stu.inventory_service.dto.response.InventoryTransactionResponse;
import com.stu.inventory_service.entity.InventoryItem;
import com.stu.inventory_service.entity.InventoryTransaction;
import com.stu.inventory_service.exception.AppException;
import com.stu.inventory_service.exception.ErrorCode;
import com.stu.inventory_service.mapper.InventoryTransactionMapper;
import com.stu.inventory_service.repository.InventoryItemRepository;
import com.stu.inventory_service.repository.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.stu.inventory_service.enums.TransactionType.*;

/**
 * Service class quản lý các giao dịch inventory (transactions)
 * Bao gồm tạo, truy vấn và quản lý lịch sử giao dịch stock
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryTransactionService {
    
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionMapper transactionMapper;
    

    @Transactional(readOnly = true)
    public InventoryTransactionResponse getTransactionById(Long id) {
        log.info("Getting transaction by ID: {}", id);
        
        InventoryTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND, 
                    "Transaction with ID " + id + " not found"));
        
        return transactionMapper.toResponse(transaction);
    }
    
    // ==================== QUERY OPERATIONS ====================
    
    /**
     * Lấy danh sách tất cả giao dịch của một inventory item
     * Sắp xếp theo thời gian tạo giảm dần (mới nhất trước)
     */
    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getTransactionsByInventoryItemId(Long inventoryItemId) {
        log.info("Getting transactions for inventory item ID: {}", inventoryItemId);
        
        List<InventoryTransaction> transactions = transactionRepository.findByInventoryItemIdOrderByCreatedAtDesc(inventoryItemId);
        return transactionMapper.toResponseList(transactions);
    }
    
    /**
     * Lấy danh sách giao dịch của một inventory item với phân trang
     * 
     * @param inventoryItemId ID của inventory item
     * @param pageable Thông tin phân trang
     * @return Page<InventoryTransactionResponse> danh sách giao dịch có phân trang
     */
    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse> getTransactionsByInventoryItemId(Long inventoryItemId, Pageable pageable) {
        log.info("Getting paginated transactions for inventory item ID: {}", inventoryItemId);
        
        Page<InventoryTransaction> transactions = transactionRepository.findByInventoryItemIdOrderByCreatedAtDesc(inventoryItemId, pageable);
        return transactions.map(transactionMapper::toResponse);
    }
    
    /**
     * Lấy danh sách giao dịch theo  orderID
     * Thường dùng để tra cứu các giao dịch liên quan đến một đơn hàng
 */
    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getTransactionsByReference(String reference) {
        log.info("Getting transactions by reference: {}", reference);
        
        List<InventoryTransaction> transactions = transactionRepository.findByReference(reference);
        return transactionMapper.toResponseList(transactions);
    }
    
    /**
     * Lấy danh sách giao dịch trong khoảng thời gian
     * 
     * @param startDate Thời gian bắt đầu
     * @param endDate Thời gian kết thúc
     * @return List<InventoryTransactionResponse> danh sách giao dịch
     */
    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting transactions between {} and {}", startDate, endDate);
        
        List<InventoryTransaction> transactions = transactionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
        return transactionMapper.toResponseList(transactions);
    }
    
    /**
     * Lấy tất cả giao dịch với phân trang
     * 
     * @param pageable Thông tin phân trang
     * @return Page<InventoryTransactionResponse> danh sách giao dịch có phân trang
     */
    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse> getAllTransactions(Pageable pageable) {
        log.info("Getting all transactions with pagination");
        
        Page<InventoryTransaction> transactions = transactionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return transactions.map(transactionMapper::toResponse);
    }
    
//    // ==================== PRIVATE HELPER METHODS ====================
//
//    /**
//     * Validate giao dịch dựa trên loại giao dịch và trạng thái inventory
//     *
//     * @param request Thông tin giao dịch cần validate
//     * @param inventoryItem Inventory item liên quan
//     * @throws AppException nếu validation thất bại
//     */
//    private void validateTransaction(InventoryTransactionRequest request, InventoryItem inventoryItem) {
//        switch (request.getTransactionType()) {
//            case RESERVE:
//                // Kiểm tra có đủ available quantity để reserve không
//                if (inventoryItem.getAvailableQuantity() < request.getQuantity()) {
//                    throw new AppException(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY,
//                        "Insufficient available quantity. Available: " + inventoryItem.getAvailableQuantity() +
//                        ", Requested: " + request.getQuantity());
//                }
//                break;
//            case SALE:
//                // Kiểm tra có đủ available quantity để bán không
//                if (inventoryItem.getAvailableQuantity() < request.getQuantity()) {
//                    throw new AppException(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY,
//                        "Insufficient available quantity. Available: " + inventoryItem.getAvailableQuantity() +
//                        ", Requested: " + request.getQuantity());
//                }
//                break;
//            case RETURN:
//                // Return luôn được phép (có thể return nhiều hơn đã bán)
//                break;
//            case ADJUSTMENT:
//                // Adjustment luôn được phép (điều chỉnh stock)
//                break;
//            default:
//                throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE,
//                    "Invalid transaction type: " + request.getTransactionType());
//        }
//    }
//
//    /**
//     * Thiết lập previous và new quantities cho transaction
//     * Dựa trên loại giao dịch để tính toán số lượng trước và sau
//     *
//     * @param transaction Giao dịch cần thiết lập
//     * @param inventoryItem Inventory item liên quan
//     */
//    private void setTransactionQuantities(InventoryTransaction transaction, InventoryItem inventoryItem) {
//        switch (transaction.getTransactionType()) {
//            case SALE:
//                // Sale: tăng sold quantity
//                transaction.setPreviousQuantity(inventoryItem.getSoldQuantity());
//                transaction.setNewQuantity(inventoryItem.getSoldQuantity() + transaction.getQuantity());
//                break;
//            case RETURN:
//                // Return: giảm sold quantity
//                transaction.setPreviousQuantity(inventoryItem.getSoldQuantity());
//                transaction.setNewQuantity(inventoryItem.getSoldQuantity() - transaction.getQuantity());
//                break;
//            case ADJUSTMENT:
//                // Adjustment: thay đổi total quantity
//                transaction.setPreviousQuantity(inventoryItem.getTotalQuantity());
//                transaction.setNewQuantity(inventoryItem.getTotalQuantity() + transaction.getQuantity());
//                break;
//        }
//    }
//
//    /**
//     * Cập nhật inventory item dựa trên giao dịch
//     * Cập nhật các quantity fields và timestamps
//     *
//     * @param transaction Giao dịch đã thực hiện
//     * @param inventoryItem Inventory item cần cập nhật
//     */
//    private void updateInventoryItem(InventoryTransaction transaction, InventoryItem inventoryItem) {
//        switch (transaction.getTransactionType()) {
//            case SALE:
//                // Cập nhật sold quantity và last sale date
//                inventoryItem.setSoldQuantity(transaction.getNewQuantity());
//                inventoryItem.setLastSaleDate(LocalDateTime.now());
//                break;
//            case RETURN:
//                // Cập nhật sold quantity
//                inventoryItem.setSoldQuantity(transaction.getNewQuantity());
//                break;
//            case ADJUSTMENT:
//                // Cập nhật total quantity
//                inventoryItem.setTotalQuantity(transaction.getNewQuantity());
//                break;
//        }
//
//        // Cập nhật available quantity và timestamp
//        inventoryItem.updateAvailableQuantity();
//        inventoryItemRepository.save(inventoryItem);
//    }
} 