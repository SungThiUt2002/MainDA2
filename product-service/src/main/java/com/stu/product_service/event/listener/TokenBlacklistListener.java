package com.stu.product_service.event.listener;

import com.stu.common_dto.event.AccountEvent.TokenBlacklistEvent;
import com.stu.product_service.entity.TokenBlacklistCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistListener {
    
    private final TokenBlacklistCache cache;

    @KafkaListener(topics = "token-blacklist", groupId = "product-service-group")
    public void handleBlacklistEvent(TokenBlacklistEvent event) {
        try {
            log.info("Received token blacklist event: jti={}, ttl={}", event.getJti(), event.getTtl());
            cache.add(event.getJti(), event.getTtl());
            log.info("Successfully added token to blacklist cache: jti={}", event.getJti());
        } catch (Exception e) {
            log.error("Error processing token blacklist event: jti={}", event.getJti(), e);
            throw e; // Re-throw để trigger error handling
        }
    }
}