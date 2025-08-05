package com.stu.common_dto.event.InventoryEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOrderEvent {
    private String orderId;
    private Long userId;
    private List<InventoryOrderItem> items;
    private LocalDateTime createdAt;
}
 // Dùng đẻ gửi event giữa order và kho