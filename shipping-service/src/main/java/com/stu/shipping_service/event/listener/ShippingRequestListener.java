package com.stu.shipping_service.event.listener;


import com.stu.common_dto.event.PaymentEvent.PaymentFailedEvent;
import com.stu.common_dto.event.PaymentEvent.PaymentSuccessEvent;
import com.stu.common_dto.event.ShippingEvent.ShippingRequestEvent;
import com.stu.shipping_service.dto.request.CreateShippingOrderRequest;
import com.stu.shipping_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShippingRequestListener {
    private final ShippingService shippingService;

    @KafkaListener(topics = "shipping-request", groupId = "shipping-service-group")
    public void createShipping(ShippingRequestEvent event){

        log.info("📦 Nhận được shipping event: {}", event); // add this

//        private Long orderId;
//        private String shippingAddress;
//        private String receiverName;
//        private String receiverPhone;
//        private String shippingMethod;
//        private String trackingCode;
//        private BigDecimal shippingFee;
//        private String note;

//        private String orderId;
//        private Long userId;
//        private String receiverName;
//        private String receiverPhone;
//        private String shippingAddress;
//        private String note;
//        private LocalDateTime paidAt;

        CreateShippingOrderRequest request= new CreateShippingOrderRequest();
        request.setOrderId(event.getOrderId());
        request.setShippingAddress(event.getShippingAddress());
        request.setReceiverName(event.getReceiverName());
        request.setReceiverPhone(event.getReceiverPhone());
        request.setNote(event.getNote());
        request.setShippingMethod("GNH"); // để mặc định trước, xử lý sau

        shippingService.createShippingOrder(request);

    }

}
