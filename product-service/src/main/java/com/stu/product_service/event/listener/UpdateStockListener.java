//package com.stu.product_service.event.listener;
//
//import com.stu.common_dto.event.AccountEvent.TokenBlacklistEvent;
//import com.stu.common_dto.event.InventoryEvent.UpdateStockEvent;
//import com.stu.product_service.service.ProductService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class UpdateStockListener {
//
//    private  final ProductService productService;
//
//    @KafkaListener(topics = "stock-update", groupId = "product-service-group")
//    public void handleUpdateStockEvent(UpdateStockEvent event) {
//        productService.updateStock(event.getProductId(),event.getNewStock(),event.getNewSold());
//    }
//
//}
