package com.stu.account_service.event;

import com.stu.common_dto.event.AccountEvent.UserStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserStatusEvent(UserStatusEvent event) {
        kafkaTemplate.send("user-events", event);
    }
    
}
