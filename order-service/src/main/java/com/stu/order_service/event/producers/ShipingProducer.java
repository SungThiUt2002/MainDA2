//package com.stu.order_service.event.producers;
//
//import com.stu.common_dto.event.ShippingEvent.ShippingRequestEvent;
//import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class ShipingProducer {
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//    public void sendShippingRequest(ShippingRequestEvent event) {
//
//        kafkaTemplate.send("shipping-request", event);
//    }
//}
