package com.stu.shipping_service.event.producer;


import com.stu.common_dto.event.ShippingEvent.ShippingStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShippingStatusProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendShippingStatuss(ShippingStatusEvent event) {

        kafkaTemplate.send("shipping-status", event);
    }

//    public void sendPaymentFailed(PaymentFailedEvent event) {
//        kafkaTemplate.send("payment-failed", event);
//    }
}
