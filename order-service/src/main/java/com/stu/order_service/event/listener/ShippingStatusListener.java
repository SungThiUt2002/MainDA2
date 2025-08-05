//package com.stu.order_service.event.listener;
//
//import com.stu.common_dto.enums.ShippingStatus;
//import com.stu.common_dto.event.InventoryEvent.InventoryOrderEvent;
//import com.stu.common_dto.event.InventoryEvent.InventoryOrderItem;
//import com.stu.common_dto.event.PaymentEvent.PaymentSuccessEvent;
//import com.stu.common_dto.event.ShippingEvent.ShippingStatusEvent;
//import com.stu.order_service.dto.request.ShippingStatusRequest;
//import com.stu.order_service.entity.Order;
//import com.stu.order_service.enums.OrderStatus;
//import com.stu.order_service.event.producers.InventoryOrderProducer;
//import com.stu.order_service.exception.AppException;
//import com.stu.order_service.exception.ErrorCode;
//import com.stu.order_service.repository.OrderRepository;
//import com.stu.order_service.service.OrderService;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class ShippingStatusListener {
//    private final OrderService orderService;
//    private final OrderRepository orderRepository;
//    private final InventoryOrderProducer inventoryOrderProducer;
//
//    @Transactional
//    @KafkaListener(topics = "shipping-status", groupId = "order-service-group")
//    public void handleShippingStatus(ShippingStatusEvent event) {
//
//        Order order = orderRepository.findById(event.getOrderId())
//                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
//
//        List<InventoryOrderItem> inventoryItems = order.getOrderItems().stream()
//                .map(item -> InventoryOrderItem.builder()
//                        .productId(item.getProductId())
//                        .quantity(item.getQuantity())
//                        .build())
//                .toList();
//
//        InventoryOrderEvent inventoryEvent = InventoryOrderEvent.builder()
//                .orderId(order.getId())
//                .userId(order.getUserId())
//                .items(inventoryItems)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        // Cạp nhập trạng thái cho đơn hàng
//        ShippingStatusRequest request = new ShippingStatusRequest();
//        switch (event.getNewStatus()) {
//            case DELIVERING ->{
//                // Đang giao hàng
//                request.setNewStatus(OrderStatus.DELIVERING);
//            }
//            case DELIVERY_SUCCESSFUL -> {
//                // Giao hàng thành công
//               request.setNewStatus(OrderStatus.DELIVERY_SUCCESSFUL);
//               // Gửi event giao hàng thàng công cho kho  --> xuất kho
//                inventoryOrderProducer.sendOrderSuccessful(inventoryEvent);
//            }
//            case RETURNED -> {
//                // Hoàn hàng
//                request.setNewStatus(OrderStatus.RETURNED);
//
//                // gửi event return cho kho
//                inventoryOrderProducer.sendReturnStockEvent(inventoryEvent);
//
//            }
//            default -> {}
//        }
//        request.setShippingOrderId(event.getShippingOrderId());
//        request.setDescription(event.getDescription());
//        request.setUpdatedAt(event.getUpdatedAt());
//        orderService.handleShippingDelivered( event.getOrderId(),request);
//    }
//
//}
