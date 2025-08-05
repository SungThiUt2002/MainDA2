package com.stu.order_service.dto.request;

import com.stu.order_service.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateSatusShippingRequest {
   private OrderStatus newStatus;
   private String description;
}
