// package com.stu.account_service.config;

// import com.stu.account_service.event.UserStatusEvent;
// import org.apache.kafka.clients.producer.ProducerConfig;
// import org.apache.kafka.common.serialization.StringSerializer;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.kafka.core.DefaultKafkaProducerFactory;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.kafka.core.ProducerFactory;
// import org.springframework.kafka.support.serializer.JsonSerializer;

// import java.util.HashMap;
// import java.util.Map;

// @Configuration
// public class KafkaProducerConfig {
//     @Bean
//     public ProducerFactory<String, UserStatusEvent> producerFactory() {
//         Map<String, Object> configProps = new HashMap<>();
//         configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092,kafka2:9092,kafka3:9092");
//         configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//         configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//         return new DefaultKafkaProducerFactory<>(configProps);
//     }

//     @Bean
//     public KafkaTemplate<String, UserStatusEvent> kafkaTemplate() {
//         return new KafkaTemplate<>(producerFactory());
//     }
// }
