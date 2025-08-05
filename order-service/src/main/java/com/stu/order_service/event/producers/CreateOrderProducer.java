package com.stu.order_service.event.producers;

import com.stu.common_dto.event.OrderEvent.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateOrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCreateOrderEvent(OrderCreatedEvent event)
    {
        kafkaTemplate.send("order-created",event);
    }

//    public void sendProductUpdateEvent(ProductUpdateEvent event) {
//        kafkaTemplate.send("product-updated", event);
//    }
//
//    public void sendProductDeleteEvent(ProductDeleteEvent event) {
//        kafkaTemplate.send("product-deleted", event);
//    }

}
