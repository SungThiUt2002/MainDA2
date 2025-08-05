//package com.stu.inventory_service.config;
//
//import org.apache.kafka.clients.admin.NewTopic;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.TopicBuilder;
//
//@Configuration
//public class KafkaConfig {
//
//    @Value("${inventory.kafka.topics.inventory-events}")
//    private String inventoryEventsTopic;
//
//    @Value("${inventory.kafka.topics.product-events}")
//    private String productEventsTopic;
//
//    @Value("${inventory.kafka.topics.cart-events}")
//    private String cartEventsTopic;
//
//    @Value("${inventory.kafka.topics.order-events}")
//    private String orderEventsTopic;
//
//    @Bean
//    public NewTopic inventoryEventsTopic() {
//        return TopicBuilder.name(inventoryEventsTopic)
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
//
//    @Bean
//    public NewTopic productEventsTopic() {
//        return TopicBuilder.name(productEventsTopic)
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
//
//    @Bean
//    public NewTopic cartEventsTopic() {
//        return TopicBuilder.name(cartEventsTopic)
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
//
//    @Bean
//    public NewTopic orderEventsTopic() {
//        return TopicBuilder.name(orderEventsTopic)
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
//}