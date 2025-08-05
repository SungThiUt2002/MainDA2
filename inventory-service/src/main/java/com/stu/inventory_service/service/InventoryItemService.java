package com.stu.inventory_service.service;

//import com.stu.common_dto.event.InventoryEvent.UpdateStockEvent;
import com.stu.inventory_service.dto.request.CreateInventoryItemRequest;
import com.stu.inventory_service.dto.request.StockImportRequest;
import com.stu.inventory_service.dto.request.UpdateProductNameRequest;
import com.stu.inventory_service.dto.response.InventoryItemResponse;
import com.stu.inventory_service.dto.response.InventoryTransactionResponse;
import com.stu.inventory_service.entity.InventoryItem;
import com.stu.inventory_service.entity.InventoryTransaction;
import com.stu.inventory_service.enums.TransactionType;
//import com.stu.inventory_service.event.producer.UpdateStockProducer;
import com.stu.inventory_service.exception.AppException;
import com.stu.inventory_service.exception.ErrorCode;
import com.stu.inventory_service.mapper.InventoryItemMapper;
import com.stu.inventory_service.mapper.InventoryTransactionMapper;
import com.stu.inventory_service.repository.InventoryItemRepository;
import com.stu.inventory_service.repository.InventoryTransactionRepository;
import com.stu.inventory_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class quản lý các thao tác liên quan đến Inventory Item
 * Bao gồm CRUD operations, stock management và business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryItemService {
    
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemMapper inventoryItemMapper;
    private final InventoryTransactionRepository inventoryTransactionRepository;
//    private final UpdateStockProducer updateStockProducer;

    private final JwtUtil jwtUtil;


    // 1. Lấy số lượng tồn kho (được gọi đến khi thêm sản phẩm vào giỏ hàng và khi tạo đơn hàng)
    @Transactional(readOnly = true)
    public Integer getAvailableQuantity(Long productId) {
        InventoryItem entity = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        return entity.getAvailableQuantity();
    }

    // 2. Hàm lấy số lượng sản phẩm ã bán (để hiển thị trên sản phẩm)
    @Transactional(readOnly = true)
    public Integer getSoldQuantity( Long productId){
        InventoryItem inventoryItem =   inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(()-> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        return inventoryItem.getSoldQuantity();
    }



    // Tạo sản phẩm mới trong kho(internal - product service)
    //2.được product service gọi để tự động tạo productVarriantId, Sku sản phẩm (Khong được nhạp trực tiếp 2 giá trị này)
     @Transactional
     public InventoryItem createInventoryItem(CreateInventoryItemRequest request) {
         log.info("Creating inventory item for product: {} ",
                 request.getProductId());
        
         //1. Validation: Kiểm tra productId đã có inventory item chưa --> đảm bảo tính duy nhất
         if (inventoryItemRepository.findItemByProductId(request.getProductId()).isPresent()) {
             throw new AppException(ErrorCode.INVENTORY_ITEM_ALREADY_EXISTS);
         }

         InventoryItem inventoryItem = inventoryItemMapper.toEntity(request);


         inventoryItem.setSoldQuantity(0);
         inventoryItem.setAvailableQuantity(inventoryItem.getTotalQuantity());
         inventoryItem.setLockedQuantity(0);
         inventoryItem.setLowStockThreshold(5);
         inventoryItem.setReorderPoint(10); // set mặc định -> cập nhập sau
         inventoryItem.setCreatedAt(LocalDateTime.now());
         inventoryItem.setIsAvailable(true);
         inventoryItem.setIsActive(true);
        
         InventoryItem savedItem = inventoryItemRepository.save(inventoryItem);
         log.info("Created inventory item with ID: {} for product: {}",
                 savedItem.getId(), savedItem.getProductId());

         // Lưu vào lịch sử
         InventoryTransaction transaction = new InventoryTransaction();
         transaction.setInventoryItem(savedItem);
         transaction.setPreviousQuantity(0);
         transaction.setNewQuantity(0);
         transaction.setQuantity(0);
         transaction.setTransactionType(TransactionType.CREATE);
         transaction.setNotes("Tạo sản phẩm từ product service");
        inventoryTransactionRepository.save(transaction);
         return savedItem;
     }

    // 3. Update thông tin sản phảm(internal - product service)
    // update tên sản phẩm --> nhận event update sản phẩm từ produc service v tự cập nhập lại
    @Transactional
    public InventoryItem updateInventoryItemByProductId(Long productId, UpdateProductNameRequest request) {
        log.info("Updating inventory item with ID: {}", productId);

        InventoryItem entity = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

        inventoryItemMapper.updateEntityFromRequest(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        InventoryItem savedEntity = inventoryItemRepository.save(entity);

        log.info("Updated inventory item with ID: {}", savedEntity.getProductId());

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setInventoryItem(savedEntity);
        transaction.setTransactionType(TransactionType.UPDATE);
        transaction.setQuantity(0);
        transaction.setPreviousQuantity(0);
        transaction.setNewQuantity(0);
        transaction.setNotes("Update sản phẩm từ product service");
        inventoryTransactionRepository.save(transaction);
        return savedEntity;
    }

    // internal(product service)
    // 4. Nhận event xóa sản phẩm tử product service --> Xóa sản phẩm trong kho tương ứng
    @Transactional
    public void deleteInventoryItemByProductId(Long productId) {
        log.info("Deleting inventory item with ID: {}", productId);

        InventoryItem entity = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

//        entity.setIsActive(false); // đánh dấu là xóa
        inventoryItemRepository.deleteByProductId(productId); // xóa hoàn toàn
//        inventoryItemRepository.save(entity);

        log.info("Deleted inventory item with ID: {}", productId);
    }
    //========================================================================================//
    // 5.nhập kho cho inventory item đã tồn tại ( đồng bộ với product-service trước)
    // chỉ nhập số lượng  và lý do nhập (admin)
    @Transactional
    public InventoryItem importStock(Long productId, StockImportRequest request, String token) {

        // 1.Giải mã token để lấy userId
        Long userId = jwtUtil.extractUserId(token);

        // 2.Kiểm tra inventory item có tồn tại không
        InventoryItem item = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

        int previousQuantity = item.getTotalQuantity();
        int newQuantity = previousQuantity + request.getQuantity();

        // 3.Tăng tồn kho
        item.setTotalQuantity(newQuantity); // tổng số lượng sản phẩm đã nhập kho
        item.setAvailableQuantity(item.getAvailableQuantity() + request.getQuantity()); // số lượng tồn kho có sẵn để bán
        item.setUpdatedAt(LocalDateTime.now());
        InventoryItem saveItem = inventoryItemRepository.save(item);


        // 4. Ghi nhận lịch sử nhập kho (transaction: ADJUSTMENT)
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setInventoryItem(item);
        transaction.setTransactionType(TransactionType.IMPORT_STOCK);
        transaction.setQuantity(request.getQuantity());
        transaction.setPreviousQuantity(previousQuantity);
        transaction.setNewQuantity(newQuantity);
        transaction.setUserId(userId);
        transaction.setNotes(request.getReason());
        transaction.setCreatedAt(LocalDateTime.now());
        inventoryTransactionRepository.save(transaction);

        return saveItem;
    }


    // 6.Hàm xuất kho (internal)
    // Xuất kho khi giao hàng thành công
    public InventoryItem exportStock( Long productId, String orderId){

        // 1. Kiểm tra xem có tồn tại không
        InventoryItem inventoryItem = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(()-> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

//        // 2. Xuất kho
//        // tổng số lượng khonong thay đổi
//        // Thay đổi số lượng sản phẩm có sẵn để bán trong kho
//        // thay đổi số lượng đã bán
//        int previousSold = inventoryItem.getSoldQuantity();
//        int previousAvailable = inventoryItem.getAvailableQuantity();
//        // Cập nhập số lượng mới
//        inventoryItem.setSoldQuantity(previousSold + exportQuantity);
//        inventoryItem.setAvailableQuantity(previousAvailable - exportQuantity);
        inventoryItem.setLastSaleDate(LocalDateTime.now());
        InventoryItem saveItem = inventoryItemRepository.save(inventoryItem);

        // 3. Lưu lịch sử
        InventoryTransaction transaction = new InventoryTransaction();
            transaction.setInventoryItem(inventoryItem);
            transaction.setTransactionType(TransactionType.EXPORT); // bán hàng thành công
            transaction.setQuantity(0); // Số  lượng thay đổi
            transaction.setPreviousQuantity(inventoryItem.getAvailableQuantity());
            transaction.setNewQuantity(saveItem.getAvailableQuantity());
//            transaction.setUserId(userId);
            transaction.setNotes("Giao hàng thành công");
            transaction.setReference(orderId);
            transaction.setCreatedAt(LocalDateTime.now());
            inventoryTransactionRepository.save(transaction);

        // 4. Gửi event cho product service
//        UpdateStockEvent event=  new UpdateStockEvent();
//            event.setProductId(saveItem.getProductId());
//            event.setNewStock(saveItem.getAvailableQuantity());
//            event.setNewSold(saveItem.getSoldQuantity());
//        updateStockProducer.sendUpdateStockEvent(event);

        return  saveItem;

    }

//========================================================//
    // 7. Dùng cho đơn thanh toán
    // Nhận event thanh toán thành công từ order service --> tự cập nhập
    // Dùng để giữ hàng, tránh oversell
    // lockedQuantity += quantity
    //availableQuantity -= quantity
    public void reserveStock(Long productId, Integer quantity, String orderId) {
        log.info("Reserving {} units for inventory item ID: {}", quantity, productId);
        
        InventoryItem item = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        
        if (item.getAvailableQuantity() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK); // không đủ hàng tồn kho
        }

        // ghi nhận đã đặt hàng
        int previousLocked = item.getLockedQuantity();
        item.setLockedQuantity(previousLocked + quantity);
        item.updateAvailableQuantity();
        inventoryItemRepository.save(item);
        log.info("Reserved {} units for inventory item ID: {}", quantity, productId);

        // ghi lại lịch sử
        InventoryTransaction transaction = new InventoryTransaction();
            transaction.setInventoryItem(item);
            transaction.setTransactionType(TransactionType.RESERVE); // đặt hàng nhưng đang chờ thanh toán
            transaction.setQuantity(quantity); // Số  lượng thay đổi
            transaction.setPreviousQuantity(previousLocked); // số lượng trước khi thay đổi
            transaction.setNewQuantity(item.getLockedQuantity()); // Số lượng sau khi thay đổi
            transaction.setReference(orderId);
            //        transaction.setUserId(userId);
            transaction.setNotes("Dự trữ chờ thanh toán");
            transaction.setCreatedAt(LocalDateTime.now());
        inventoryTransactionRepository.save(transaction);

//        // gửi vent cập nhập tôn fkho cho  rpdoduct service
//        UpdateStockEvent event = new UpdateStockEvent();
//        event.setProductId(productId);
//        event.setNewStock(item.getAvailableQuantity());
//        event.setNewSold(item.getSoldQuantity());
//        updateStockProducer.sendUpdateStockEvent(event);

    }

    // 8. Giải phóng stock đã đặt trước,Trả lại hàng đã giữ chỗ
    // Khi đơn pending bị huỷ hoặc timeout tỏng thời gian thanh toán
    public void releaseReservedStock(Long productId, Integer quantity, String orderId) {
        log.info("Releasing {} units for inventory item ID: {}", quantity, productId);

        InventoryItem entity = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

        // Đảm bảo không giảm reserved quantity xuống dưới 0
        int newReservedQuantity = Math.max(0, entity.getLockedQuantity() - quantity);
        entity.setLockedQuantity(newReservedQuantity);
        entity.updateAvailableQuantity();
        entity.setUpdatedAt(LocalDateTime.now());
        inventoryItemRepository.save(entity);

        // Lưu lịch sử
        InventoryTransaction transaction = new InventoryTransaction();
            transaction.setInventoryItem(entity);
            transaction.setTransactionType(TransactionType.RELEASE); // đặt hàng nhưng đang chờ thanh toán
            transaction.setQuantity(quantity); // Số  lượng thay đổi
            transaction.setPreviousQuantity(newReservedQuantity); // số lượng trước khi thay đổi
            transaction.setNewQuantity(entity.getLockedQuantity()); // Số lượng sau khi thay đổi
            transaction.setReference(orderId);
            //        transaction.setUserId(userId);
            transaction.setNotes("Hủy đặt hàng");
            transaction.setCreatedAt(LocalDateTime.now());
        inventoryTransactionRepository.save(transaction);

//        // gửi event cập nhạp số lượng tồn kho cho product service
//        UpdateStockEvent event = new UpdateStockEvent();
//        event.setProductId(productId);
//        event.setNewStock(entity.getAvailableQuantity());
//        event.setNewSold(entity.getSoldQuantity());
//        updateStockProducer.sendUpdateStockEvent(event);

        log.info("Released {} units for inventory item ID: {}", quantity, productId);
    }
    
    // 9.(internal)
    // Xác nhận đơn hàng đã giữ hàng trước đó (locked) khi thanh toán thành công
    // Khi đơn hàng có trạng thái PENDING và đã lock tồn kho từ trước
    // Hoàn tất đơn hàng → chuyển từ locked sang sold
    // lockedQuantity -= quantity
    //soldQuantity += quantity
    //availableQuantity update
    public void sellReservedStock(Long productId, Integer quantity, String orderId) {
        log.info("Selling {} units for inventory item ID: {}", quantity, productId);

        InventoryItem entity = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

        // Kiểm tra xem đã đặt hàng chưa
        if (entity.getLockedQuantity() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_RESERVED_QUANTITY,
                "Insufficient reserved stock. Reserved: " + entity.getLockedQuantity() + ", Requested: " + quantity);
        }

        int previousSold =  entity.getSoldQuantity();
        int previousLocked = entity.getLockedQuantity();

        entity.setSoldQuantity(previousSold + quantity);
        entity.setLockedQuantity(previousLocked - quantity);
        entity.updateAvailableQuantity();

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setLastSaleDate(LocalDateTime.now());
        inventoryItemRepository.save(entity);
        log.info("Sold {} units for inventory item ID: {}", quantity, productId);

        // ghi nhận lịch sử
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setInventoryItem(entity);
        transaction.setTransactionType(TransactionType.SALE); // đặt hàng nhưng đang chờ thanh toán
        transaction.setQuantity(quantity); // Số  lượng thay đổi
        transaction.setPreviousQuantity(previousSold); // số lượng trước khi thay đổi
        transaction.setNewQuantity(entity.getSoldQuantity()); // Số lượng sau khi thay đổi
        transaction.setReference(orderId);
        //        transaction.setUserId(userId);
        transaction.setNotes("Thanh toán thành công");
        transaction.setCreatedAt(LocalDateTime.now());
        inventoryTransactionRepository.save(transaction);

//        // gửi event cập nhập số lượng cho product service
//        UpdateStockEvent event = new UpdateStockEvent();
//        event.setProductId(productId);
//        event.setNewStock(entity.getAvailableQuantity());
//        event.setNewSold(entity.getSoldQuantity());
//        updateStockProducer.sendUpdateStockEvent(event);

    }
    
    /**
     * 10. Trả hàng (return), hủy đơn hàng đã xác nhận(Đã SALE)
     * Tăng available quantity
     * Giảm số lượng sản phẩm đã bán
    */
    @Transactional
    public void returnStock(Long productId, Integer quantity, String orderId) {

        log.info("Returning {} units for inventory item ID: {}", quantity, productId);
        InventoryItem entity = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

        // Cập nhập lại số lượng sản phẩm trong kho
        int previousSold = entity.getSoldQuantity();
        int previousAvailable = entity.getAvailableQuantity();
        entity.setSoldQuantity(previousSold - quantity);
        entity.updateAvailableQuantity();
        inventoryItemRepository.save(entity);
        log.info("Returned {} units for inventory item ID: {}", quantity, productId);

        // ghi nhậ lịch sử
        InventoryTransaction transaction = new InventoryTransaction();
            transaction.setInventoryItem(entity);
            transaction.setTransactionType(TransactionType.RETURN); // trả hàng
            transaction.setQuantity(quantity); // Số  lượng thay đổi
            transaction.setPreviousQuantity(previousAvailable); // số lượng trước khi thay đổi
            transaction.setNewQuantity(entity.getAvailableQuantity()); // Số lượng sau khi thay đổi
            transaction.setReference(orderId);
            //        transaction.setUserId(userId);
            transaction.setNotes("Hủy đơn hàng");
            transaction.setCreatedAt(LocalDateTime.now());
        inventoryTransactionRepository.save(transaction);

//        // gủi event cập nhập số lượng tồn kho cho product service
//        UpdateStockEvent event = new UpdateStockEvent();
//        event.setProductId(productId);
//        event.setNewStock(entity.getAvailableQuantity());
//        event.setNewSold(entity.getSoldQuantity());
//        updateStockProducer.sendUpdateStockEvent(event);
    }
    
    /**
     * 11. Điều chỉnh stock (adjustment)(chỉ điều chỉnh được số lượng nahpaj kho)
     * Thay đổi total quantity theo lý do điều chỉnh
     * điều chỉnh thủ công bởi admin
     */
    public void adjustStock(Long productId, Integer quantity, String reason) {
        log.info("Adjusting stock by {} units for inventory item ID: {} - Reason: {}", quantity, productId, reason);
        
        InventoryItem entity = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

        // điều chỉnh
        int previousTotal = entity.getTotalQuantity();
        entity.setTotalQuantity(previousTotal+ quantity);
        entity.updateAvailableQuantity();
        inventoryItemRepository.save(entity);
        log.info("Adjusted stock by {} units for inventory item ID: {}", quantity, productId);

        // ghi nhậ lịch sử
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setInventoryItem(entity);
        transaction.setTransactionType(TransactionType.ADJUSTMENT); // trả hàng
        transaction.setQuantity(quantity); // Số  lượng thay đổi
        transaction.setPreviousQuantity(previousTotal); // số lượng trước khi thay đổi
        transaction.setNewQuantity(entity.getTotalQuantity()); // Số lượng sau khi thay đổi
//        transaction.setReference(orderId);
        //        transaction.setUserId(userId);
        transaction.setNotes("Điều chỉnh nhập sô lượng nhập kho");
        transaction.setCreatedAt(LocalDateTime.now());
        inventoryTransactionRepository.save(transaction);

//        // gửi event cập nhập tồn kho cho product service
//        UpdateStockEvent event = new UpdateStockEvent();
//        event.setProductId(productId);
//        event.setNewStock(entity.getAvailableQuantity());
//        event.setNewSold(entity.getSoldQuantity());
//        updateStockProducer.sendUpdateStockEvent(event);
    }
    
    /**
     * 12. Kiểm tra xem có đủ stock không
     * 
     * @param inventoryItemId ID của inventory item
     * @param quantity Số lượng cần kiểm tra
     * @return true nếu đủ stock, false nếu không đủ
     */
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long inventoryItemId, Integer quantity) {
        InventoryItem entity = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        
        return entity.getAvailableQuantity() >= quantity;
    }


    // 14. Lấy thông tin inventory item theo Product ID
    @Transactional(readOnly = true)
    public InventoryItem getInventoryItemByProductId(Long productId) {
        log.info("Getting inventory item by product variant ID: {}", productId);

        InventoryItem item = inventoryItemRepository.findItemByProductId(productId)
                .orElseThrow(()-> new  AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

        return item;
    }


    // 15. Lấy danh sách tất cả inventory items đang active =true
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getAllActiveInventoryItems() {
        log.info("Getting all active inventory items");

        List<InventoryItem> entities = inventoryItemRepository.findByIsActiveTrue();
        return inventoryItemMapper.toResponseList(entities);
    }


    // 16. Lấy danh sách inventory items có sẵn (available>0 và active =true)
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getAvailableInventoryItems() {
        log.info("Getting available inventory items");

        List<InventoryItem> entities = inventoryItemRepository.findByIsAvailableTrueAndIsActiveTrue();
        return inventoryItemMapper.toResponseList(entities);
    }


    // 17.Lấy danh sách inventory items có stock thấp ( sản phẩm tồn kho <  ngưững cảng báo
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getLowStockItems() {
        log.info("Getting low stock items");

        List<InventoryItem> entities = inventoryItemRepository.findLowStockItems();
        return inventoryItemMapper.toResponseList(entities);
    }


    // 18.Lấy danh sách inventory items cần đặt hàng lại (reorder)
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getItemsNeedingReorder() {
        log.info("Getting items needing reorder");

        List<InventoryItem> entities = inventoryItemRepository.findItemsNeedingReorder();
        return inventoryItemMapper.toResponseList(entities);
    }
} 