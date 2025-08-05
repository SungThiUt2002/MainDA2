package com.stu.common_dto.event.ShippingEvent;

import com.stu.common_dto.enums.ShippingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingStatusEvent {
    private String orderId;
    private Long shippingOrderId;
    private ShippingStatus newStatus;       // DELIVERED, FAILED, RETURNED, CANCELLED
    private LocalDateTime updatedAt;
    private String description;             // Lý do/ghi chú trạng thái
}
