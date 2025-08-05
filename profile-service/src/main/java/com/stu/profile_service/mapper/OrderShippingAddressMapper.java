package com.stu.profile_service.mapper;

import com.stu.profile_service.dto.request.OrderShippingAddressRequest;
import com.stu.profile_service.dto.response.OrderShippingAddressResponse;
import com.stu.profile_service.entity.OrderShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderShippingAddressMapper {

//    OrderShippingAddressMapper INSTANCE = Mappers.getMapper(OrderShippingAddressMapper.class);

    @Mapping(target = "id", ignore = true)
    OrderShippingAddress toEntity(OrderShippingAddressRequest request);

    OrderShippingAddressResponse toResponse(OrderShippingAddress entity);

    List<OrderShippingAddressResponse> toResponseList(List<OrderShippingAddress> entities);
}
