package com.stu.order_service.mapper;

import com.stu.order_service.dto.request.OrderItemRequest;
import com.stu.order_service.dto.response.OrderItemResponse;
import com.stu.order_service.entity.OrderItem;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemResponse toOrderItemResponse(OrderItem item);
    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items);

    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "productPrice", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "id", ignore = true)
    OrderItem toOrderItem(OrderItemRequest request);
    List<OrderItem> toOrderItemList(List<OrderItemRequest> requests);
} 