package com.stu.payment_service.event.producer;

import com.stu.common_dto.event.PaymentEvent.PaymentFailedEvent;
import com.stu.common_dto.event.PaymentEvent.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentSuccess(PaymentSuccessEvent event) {

        kafkaTemplate.send("payment-success",event.getOrderId(), event);
    }

    public void sendPaymentFailed(PaymentFailedEvent event) {

        kafkaTemplate.send("payment-failed", event.getOrderId(), event);
    }
}
