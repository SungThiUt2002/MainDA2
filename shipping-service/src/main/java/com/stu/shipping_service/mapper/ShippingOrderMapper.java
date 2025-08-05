package com.stu.shipping_service.mapper;

import com.stu.shipping_service.entity.ShippingOrder;
import com.stu.shipping_service.dto.request.CreateShippingOrderRequest;
import com.stu.shipping_service.dto.response.ShippingOrderResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ShippingOrderMapper {
    ShippingOrderResponse toResponse(ShippingOrder order);

    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "trackingCode", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "shippingFee", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ShippingOrder toEntity(CreateShippingOrderRequest request);
} 