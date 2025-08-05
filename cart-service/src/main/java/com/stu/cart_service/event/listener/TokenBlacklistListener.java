package com.stu.cart_service.event.listener;

import com.stu.cart_service.entity.TokenBlacklistCache;
import com.stu.common_dto.event.AccountEvent.TokenBlacklistEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TokenBlacklistListener {
    private final TokenBlacklistCache cache;

    public TokenBlacklistListener(TokenBlacklistCache cache) {
        this.cache = cache;
    }

    @KafkaListener(topics = "token-blacklist", groupId = "cart-service-group")
    public void handleBlacklistEvent(TokenBlacklistEvent event) {
        cache.add(event.getJti(), event.getTtl());
    }
}