package com.stu.cart_service.mapper;

import com.stu.cart_service.dto.request.CartItemRequest;
import com.stu.cart_service.dto.response.CartItemResponse;
import com.stu.cart_service.entity.CartItem;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "cart", ignore = true)
    CartItem toEntity(CartItemRequest request);

    CartItemResponse toResponse(CartItem entity);

    List<CartItemResponse> toResponseList(List<CartItem> entities);
}
