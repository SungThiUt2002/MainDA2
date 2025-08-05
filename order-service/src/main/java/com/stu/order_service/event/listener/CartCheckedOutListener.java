package com.stu.order_service.event.listener;

import com.stu.common_dto.enums.PaymentMethod;
import com.stu.common_dto.event.CartEvent.CartCheckedOutEvent;
import com.stu.common_dto.event.CartEvent.CartItemDTO;
import com.stu.common_dto.event.PaymentEvent.PaymentSuccessEvent;
import com.stu.order_service.dto.request.CreateOrderRequest;
import com.stu.order_service.dto.request.OrderItemRequest;
import com.stu.order_service.entity.Order;
import com.stu.order_service.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartCheckedOutListener {
    private final OrderService orderService;

    @Transactional
    @KafkaListener(topics = "cart-checkout", groupId = "order-service-group")
    public void handleCartCheckedOut(CartCheckedOutEvent event) {

        log.info("📥 Nhận cart checkout event từ userId = {}", event.getUserId());

        try {
            // 1. Map CartCheckedOutEvent → CreateOrderRequest
            CreateOrderRequest request = new CreateOrderRequest();
            request.setUserId(event.getUserId());
            request.setItems(event.getItems().stream()
                    .map(item -> OrderItemRequest.builder()
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .productPrice(item.getPrice())
                            .quantity(item.getQuantity())
                            .build())
                    .toList());

            // 2. Gọi service createOrder
            Order order = orderService.createOrder(request);

            log.info("✅ Tạo đơn hàng thành công: {}", order.getId());

        } catch (Exception e) {
            log.error(" Lỗi khi tạo order từ event cart-checkout", e);
            // Bạn có thể retry hoặc gửi sang dead-letter-topic tại đây nếu cần
            throw e;
        }


    }
}
