package com.stu.account_service.event;

import com.stu.common_dto.event.AccountEvent.TokenBlacklistEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class TokenBlacklistPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TokenBlacklistPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBlacklistEvent(String jti, long ttlSeconds) {
        TokenBlacklistEvent event = new TokenBlacklistEvent(jti, ttlSeconds);
        kafkaTemplate.send("token-blacklist", event);
    }
}