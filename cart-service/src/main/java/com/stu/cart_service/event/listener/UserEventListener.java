package com.stu.cart_service.event.listener;

import com.stu.cart_service.service.CartService;
import com.stu.common_dto.event.AccountEvent.UserStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {
    private final CartService cartService;

    @KafkaListener(topics = "user-events", groupId = "cart-service-group")
    public void handleUserStatusEvent(UserStatusEvent event) {
        log.info("Received user event: {} for userId: {}", event.getAction(), event.getUserId());
        if ("DELETED".equalsIgnoreCase(event.getAction())) {
            cartService.deleteCartByUserId(event.getUserId());
        } else if ("DISABLED".equalsIgnoreCase(event.getAction())) {
            cartService.disableCartByUserId(event.getUserId());
        }
    }
}
