package com.stu.cart_service.event.listener;

import com.stu.cart_service.service.CartItemService;
import com.stu.common_dto.event.ProductEvent.ProductDeleteEvent;
import com.stu.common_dto.event.ProductEvent.ProductUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductListener {

    private final CartItemService cartItemService;

    @KafkaListener(topics = "product-updated", groupId = "cart-service-group")
    public void handleProductUpdated(ProductUpdateEvent event) {
        log.info("Nhận event product updated: productId={}",
                event.getProductId());
        // Validate event data
        if (event.getProductId() == null) {
            log.error("Event data không hợp lệ: productId={}",
                    event.getProductId());
            return;
        }
        try {
            // Gọi service layer để cập nhật cart items
            cartItemService.updateCartItemFromProductEvent(event.getProductId(), event.getNewPrice(), event.getProductName());
        } catch (Exception e) {
            log.error("Lỗi khi xử lý product update event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "product-deleted", groupId = "cart-service-group")
    public void handleProductDeleted(ProductDeleteEvent event) {
        log.info("Nhận event xóa variant: productId={}", event.getProductId());
        try {
            cartItemService.markCartItemsAsRemovedByVariantId(event.getProductId());
        } catch (Exception e) {
            log.error("Lỗi khi xử lý product delete event: {}", e.getMessage(), e);
        }
    }
} 