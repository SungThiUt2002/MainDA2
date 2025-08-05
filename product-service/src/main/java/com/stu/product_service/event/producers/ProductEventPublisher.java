package com.stu.product_service.event.producers;

import com.stu.common_dto.event.ProductEvent.ProductCreatEvent;
import com.stu.common_dto.event.ProductEvent.ProductDeleteEvent;
import com.stu.common_dto.event.ProductEvent.ProductUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendProductCreateEvent(ProductCreatEvent event) {

        kafkaTemplate.send("product-created", event);
    }

    public void sendProductUpdateEvent(ProductUpdateEvent event) {

        kafkaTemplate.send("product-updated", event);
    }

    public void sendProductDeleteEvent(ProductDeleteEvent event) {

        kafkaTemplate.send("product-deleted", event);
    }


}