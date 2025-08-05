package com.stu.common_dto.event.InventoryEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOrderItem {
    private Long productId;
    private Integer quantity;
}
