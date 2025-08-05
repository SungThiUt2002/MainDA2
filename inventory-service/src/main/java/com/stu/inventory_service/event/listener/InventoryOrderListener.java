package com.stu.inventory_service.event.listener;


import com.stu.common_dto.event.InventoryEvent.InventoryOrderEvent;
import com.stu.common_dto.event.InventoryEvent.InventoryOrderItem;
import com.stu.inventory_service.service.InventoryItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryOrderListener {
    private final InventoryItemService inventoryItemService;

    @KafkaListener(topics = "inventory-reserve", groupId = "inventory-service-group")
    public void handleReserveStock(InventoryOrderEvent event) {
        for (InventoryOrderItem item : event.getItems()){
            try {
                inventoryItemService.reserveStock(item.getProductId(), item.getQuantity(), event.getOrderId());
            } catch (Exception ex) {
                log.error("Lỗi khi xác nhận đặt hàng cho sản phẩm {}: {}", item.getProductId(), ex.getMessage());
            }

        }
    }

    @KafkaListener(topics = "inventory-sell", groupId = "inventory-service-group")
    public void handlePaymentSuccess(InventoryOrderEvent event) {
        log.info("Nhận PaymentSucceededEvent: {}", event);
        for (InventoryOrderItem item : event.getItems()) {
            try {
                inventoryItemService.sellReservedStock(item.getProductId(), item.getQuantity(), event.getOrderId());
            } catch (Exception ex) {
                log.error("Lỗi khi xác nhận bán hàng cho sản phẩm {}: {}", item.getProductId(), ex.getMessage());
            }
        }
    }

    @KafkaListener(topics = "inventory-release", groupId = "inventory-service-group")
    public void handlePaymentFailed(InventoryOrderEvent event) {
        log.info("Nhận PaymentFailedEvent: {}", event);
        for (InventoryOrderItem item : event.getItems()) {
            try {
                inventoryItemService.releaseReservedStock(item.getProductId(), item.getQuantity(), event.getOrderId());
            } catch (Exception ex) {
                log.error(" Lỗi khi giải phóng tồn kho cho sản phẩm {}: {}", item.getProductId(), ex.getMessage());
            }
        }
    }

    // Nhận event hủy đơn hàng
    @KafkaListener(topics = "inventory-return", groupId = "inventory-service-group")
    public void handleReturnOrder(InventoryOrderEvent event) {
        log.info("Nhận event hủy đơn hàng  từ order: {}", event.getOrderId());
        for (InventoryOrderItem item : event.getItems()) {
            try {
                inventoryItemService.returnStock(item.getProductId(), item.getQuantity(), event.getOrderId());
            } catch (Exception ex) {
                log.error(" Lỗi khi giải phóng tồn kho cho sản phẩm {}: {}", item.getProductId(), ex.getMessage());
            }
        }
    }

    // Nhận event giao hàng thành công từ order
    @KafkaListener(topics = "inventory-export", groupId = "inventory-service-group")
    public void handleExport(InventoryOrderEvent event) {
        log.info("Nhận event giao hàng thành công từ order: {}", event.getOrderId());
        for (InventoryOrderItem item : event.getItems()) {
            try {
                inventoryItemService.exportStock(item.getProductId(), event.getOrderId());
            } catch (Exception ex) {
                log.error(" Lỗi khi Xuất kho sản phẩm {}: {}", item.getProductId(), ex.getMessage());
            }
        }
    }
}
