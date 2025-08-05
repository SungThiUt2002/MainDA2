package com.stu.order_service.mapper;

import com.stu.order_service.dto.request.CreateOrderRequest;
import com.stu.order_service.dto.request.UpdateOrderInfoRequest;
import com.stu.order_service.dto.response.OrderResponse;
import com.stu.order_service.entity.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {


    @Mapping(target = "items", source = "orderItems")
    OrderResponse toOrderResponse(Order order);


    @Mapping(target = "ward", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "streetAddress", ignore = true)
    @Mapping(target = "statusHistories", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "shippingId", ignore = true)
    @Mapping(target = "shippingFee", ignore = true)
    @Mapping(target = "receiverPhone", ignore = true)
    @Mapping(target = "receiverName", ignore = true)
    @Mapping(target = "province", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "note", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "finalAmount", ignore = true)
    @Mapping(target = "district", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    Order toOrder(CreateOrderRequest request);



   @Mapping(target = "ward", ignore = true)
   @Mapping(target = "userId", ignore = true)
   @Mapping(target = "updatedAt", ignore = true)
   @Mapping(target = "totalAmount", ignore = true)
   @Mapping(target = "streetAddress", ignore = true)
   @Mapping(target = "statusHistories", ignore = true)
   @Mapping(target = "status", ignore = true)
   @Mapping(target = "shippingId", ignore = true)
   @Mapping(target = "shippingFee", ignore = true)
   @Mapping(target = "receiverPhone", ignore = true)
   @Mapping(target = "receiverName", ignore = true)
   @Mapping(target = "province", ignore = true)
   @Mapping(target = "paymentId", ignore = true)
   @Mapping(target = "orderItems", ignore = true)
   @Mapping(target = "id", ignore = true)
   @Mapping(target = "finalAmount", ignore = true)
   @Mapping(target = "district", ignore = true)
   @Mapping(target = "discountAmount", ignore = true)
   @Mapping(target = "createdAt", ignore = true)
   void updateOrderFromDto(UpdateOrderInfoRequest request, @MappingTarget Order order);

} 