package com.stu.payment_service.event.listener;

import com.stu.common_dto.enums.PaymentMethod;
import com.stu.common_dto.event.OrderEvent.OrderCreatedEvent;
import com.stu.common_dto.event.PaymentEvent.PaymentFailedEvent;
import com.stu.common_dto.event.PaymentEvent.PaymentSuccessEvent;
import com.stu.payment_service.dto.request.CreatePaymentRequest;
import com.stu.payment_service.dto.response.PaymentResponse;
import com.stu.payment_service.enums.PaymentStatus;

import com.stu.payment_service.event.producer.PaymentProducer;
import com.stu.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CreateOrderListener {
    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentProducer paymentKafkaProducer;

    @KafkaListener(topics = "order-created", groupId = "payment-service-group")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 1. Tạo request cho PaymentService
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(event.getOrderId());
        request.setUserId(event.getUserId());
        request.setAmount(event.getTotalAmount());
        request.setCurrency("VND"); // hoặc lấy từ event nếu có
        request.setPaymentMethod(event.getPaymentMethod()); // Lấy từ event, dùng enum chung

        // 2. Gọi service để tạo payment
        paymentService.createPayment(request);

    }
}

//private String orderId;               // ID đơn hàng
//private Long paymentId;              // ID thanh toán
//private Long userId;                 // ID người dùng
//private BigDecimal amount;           // Số tiền đã thanh toán
//private PaymentMethod paymentMethod; // Phương thức thanh toán
//private LocalDateTime paidAt;        // Thời điểm xác nhận thanh toán