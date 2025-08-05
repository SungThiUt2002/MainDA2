package com.stu.inventory_service.event.listener;


import com.stu.common_dto.event.ProductEvent.ProductCreatEvent;
import com.stu.common_dto.event.ProductEvent.ProductDeleteEvent;
import com.stu.common_dto.event.ProductEvent.ProductUpdateEvent;
import com.stu.inventory_service.dto.request.CreateInventoryItemRequest;
import com.stu.inventory_service.dto.request.UpdateProductNameRequest;
import com.stu.inventory_service.service.InventoryItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductListener {
    private final InventoryItemService inventoryItemService;

    @KafkaListener(topics = "product-created", groupId = "inventory-service-group")
    public void handleCreateProductEvent(ProductCreatEvent event) {

        CreateInventoryItemRequest request = new CreateInventoryItemRequest();
        request.setProductId(event.getProductId());
        request.setProductName(event.getProductName());
        inventoryItemService.createInventoryItem(request);

    }

    @KafkaListener(topics = "product-updated", groupId = "inventory-service-group")
    public void handleUpdateProductEvent(ProductUpdateEvent event) {
        UpdateProductNameRequest request = new UpdateProductNameRequest();
        request.setProductName(event.getProductName());
        inventoryItemService.updateInventoryItemByProductId(event.getProductId(),request);

    }

    // Xóa sản phẩm trong kho tuương ứng khi nhận dc evetn xóa sản phẩm từ product service
    @KafkaListener(topics = "product-deleted", groupId = "inventory-service-group")
    public void handleProductDeleted(ProductDeleteEvent event) {
        log.info("Nhận event xóa productId={}", event.getProductId());
        try {
            inventoryItemService.deleteInventoryItemByProductId(event.getProductId());
        } catch (Exception e) {
            log.error("Lỗi khi xử lý product delete event: {}", e.getMessage(), e);
        }
    }

}
