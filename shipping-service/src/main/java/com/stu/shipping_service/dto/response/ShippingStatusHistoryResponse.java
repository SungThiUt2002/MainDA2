package com.stu.shipping_service.dto.response;

import com.stu.common_dto.enums.ShippingStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShippingStatusHistoryResponse {
    private Long id;
    private ShippingStatus status;
    private LocalDateTime changedAt;
    private String description;
} 