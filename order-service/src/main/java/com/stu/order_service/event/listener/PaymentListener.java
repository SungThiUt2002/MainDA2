//package com.stu.order_service.event.listener;
//
//
//import com.stu.common_dto.event.PaymentEvent.PaymentFailedEvent;
//import com.stu.common_dto.event.PaymentEvent.PaymentSuccessEvent;
//import com.stu.order_service.entity.Order;
//import com.stu.order_service.service.OrderService;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class PaymentListener {
//
//    private final OrderService orderService;
//
//    @KafkaListener(topics = "payment-success", groupId = "order-service-group")
//    public void handlePaymentSuccess(PaymentSuccessEvent event) {
//        orderService.handlePaymentSuccess(event);
//    }
//
//    @KafkaListener(topics = "payment-failed", groupId = "order-service-group")
//    public void handlePaymentFailed(PaymentFailedEvent event) {
//        orderService.handlePaymentFailed(event );
//    }
//}
