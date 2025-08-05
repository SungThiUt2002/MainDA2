package com.stu.shipping_service.mapper;

import com.stu.shipping_service.entity.ShippingStatusHistory;
import com.stu.shipping_service.dto.response.ShippingStatusHistoryResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ShippingStatusHistoryMapper {
    ShippingStatusHistoryResponse toResponse(ShippingStatusHistory history);
} 