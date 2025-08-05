package com.stu.shipping_service.dto.request;

import com.stu.common_dto.enums.ShippingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateSatusShippingRequest {
   private ShippingStatus newStatus;
   private String description;
}
