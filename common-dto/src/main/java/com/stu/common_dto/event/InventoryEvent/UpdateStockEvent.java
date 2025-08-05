package com.stu.common_dto.event.InventoryEvent;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStockEvent {
    private Long productId;
    private Integer newStock;
    private Integer newSold;
}
