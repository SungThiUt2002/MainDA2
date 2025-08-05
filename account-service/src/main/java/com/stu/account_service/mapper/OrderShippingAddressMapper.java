package com.stu.account_service.mapper;

import com.stu.account_service.dto.request.OrderShippingAddressRequest;
import com.stu.account_service.dto.response.OrderShippingAddressResponse;
import com.stu.account_service.entity.OrderShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderShippingAddressMapper {

    @Mapping(target = "id", ignore = true)
    OrderShippingAddress toEntity(OrderShippingAddressRequest request);

    OrderShippingAddressResponse toResponse(OrderShippingAddress entity);

    List<OrderShippingAddressResponse> toResponseList(List<OrderShippingAddress> entities);
}
