package com.stu.order_service.event.producers;

import com.stu.common_dto.event.InventoryEvent.InventoryOrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryOrderProducer {
    private final KafkaTemplate<String, Object> kafka;

    public void sendInventoryReserveEvent(InventoryOrderEvent event) {
        kafka.send("inventory-reserve", event.getOrderId(), event);
    }

    public void sendSellReservedStock(InventoryOrderEvent event) {
        kafka.send("inventory-sell", event.getOrderId(), event);
    }

    public void sendPaymentFailForStock(InventoryOrderEvent event) {
        kafka.send("inventory-release", event.getOrderId(), event);
    }
    // Hủy đơn hàng, hoàn hàng
    public void sendReturnStockEvent(InventoryOrderEvent event) {
        kafka.send("inventory-return", event.getOrderId(), event);
    }

    // event giao hàng thành công
    public void sendOrderSuccessful(InventoryOrderEvent event) {
        kafka.send("inventory-export", event.getOrderId(), event);
    }
}
