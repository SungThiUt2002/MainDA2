package com.stu.cart_service.event.producer;

import com.stu.common_dto.event.CartEvent.CartCheckedOutEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartCheckedOutEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCartCheckedOutEvent(CartCheckedOutEvent event) {

        kafkaTemplate.send("cart-checkout", event);
    }

}
